package com.gabmene.videoguesser.listener;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.repository.RoomRepository;
import com.gabmene.videoguesser.service.RoomService;
import com.gabmene.videoguesser.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserSessionManager {

    private final RoomService roomService;
    private final RoomRepository roomRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Map<Integer, ScheduledFuture<?>> pendingRoomDisconnects = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledFuture<?>> pendingUserDeletions = new ConcurrentHashMap<>();
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if(principal != null){

            Integer userId = Integer.parseInt(principal.getName());
            log.warn("Received a new web socket connection: {}", userId);

            ScheduledFuture<?> pendingUserDeletion = pendingUserDeletions.remove(userId);

            boolean wasRescued = false;

            if(pendingUserDeletion != null){
                pendingUserDeletion.cancel(false);
                wasRescued = true;
            }
            if (wasRescued) {
                log.warn("✅ User {} connected to WebSocket - canceled deletion", userId);
            } else {
                log.warn("🔌 User {} connected to WebSocket for the first time!", userId);
            }
        }
    }

    @EventListener
    public void handleUserSubscribeEvent(SessionSubscribeEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String destination = accessor.getDestination();
        if(principal != null && (destination != null && destination.startsWith("/topic/room/"))){
            Integer userId = Integer.parseInt(principal.getName());
            String roomCode = destination.split("/")[3];

            roomRepository.findRoomCodeByUserId(userId).ifPresentOrElse(userRoomCode -> {
                    if(roomCode.equals(userRoomCode)){
                        ScheduledFuture<?> pendingRoomDisconnect = pendingRoomDisconnects.remove(userId);

                        if(pendingRoomDisconnect != null){
                            pendingRoomDisconnect.cancel(false);
                            log.warn("✅ User {} reconnected to room {} in time!", userId , roomCode);
                        }
                    } else {
                        log.warn("User {} tried to subscribe to room {} but is not in it, belongs to {}. Forcing disconnect", userId, roomCode, userRoomCode);
                        ScheduledFuture<?> pendingRoomDisconnect = pendingRoomDisconnects.remove(userId);
                        if(pendingRoomDisconnect != null){
                            pendingRoomDisconnect.cancel(false);
                        }

                        try {
                            roomService.handleRoomDisconnect(userId);
                            log.warn("User {} disconnected from old room", userId);
                        } catch (Exception e) {
                            log.error("Error handling user disconnect: {}", e.getMessage());
                        }
                    }
                },()->{
                    log.warn("User {} tried to subscribe to room {} but has no room assigned in DB.", userId, roomCode);
                });
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.warn("Received a WebScoket disconnect event");
        StompHeaderAccessor acessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = acessor.getUser();
        if(principal != null){

            Integer userId = Integer.parseInt(principal.getName());

            // if the user didn't leave the room via button, only closed the tab, the user would still be in the room.
            if(roomRepository.findRoomCodeByUserId(userId).isPresent()){
                log.warn("User Disconnected: {}, {} seconds before disconnecting from room", userId, AppConstants.TIME_FOR_ROOM_DISCONNECT_SECONDS);

                ScheduledFuture<?> roomDisconnectFuture = scheduler.schedule(()->{
                    pendingRoomDisconnects.remove(userId);

                    try {
                        roomService.handleRoomDisconnect(userId);
                        log.warn("User {} disconnected from his old room", userId);
                    } catch (Exception e) {
                        log.error("Error handling user disconnect: {}", e.getMessage());
                    }

                }, AppConstants.TIME_FOR_ROOM_DISCONNECT_SECONDS, TimeUnit.SECONDS);

                pendingRoomDisconnects.put(userId, roomDisconnectFuture);

                // If he left the room via button, this timer should be skipped since the user would be redirected to home page.
                // Which means this timer would be called already through /me.
                this.resetUserDeletionTimer(userId);
            }


        }

    }

    // If guest, after its creation, doesn't connect to WebSocket in 10 minutes, delete him -- called in POST /guest
    public void scheduleInitialDestruction(Integer userId) {
        log.warn("Scheduling deletion for user {}, has {} minutes to connect to WebSocket", userId, AppConstants.TIME_BEFORE_FIRST_GUEST_DELETION_MINUTES);

        ScheduledFuture<?> userDeletionFuture = scheduler.schedule(()->{
            pendingUserDeletions.remove(userId);

            try {
                userService.handleUserDisconnect(userId);
                log.warn("User {} deleted, didn't connect to WebSocket", userId);
            } catch (Exception e) {
                log.error("Error handling zombie user deletion: {}", e.getMessage());
            }
        }, AppConstants.TIME_BEFORE_FIRST_GUEST_DELETION_MINUTES, TimeUnit.MINUTES);

        pendingUserDeletions.put(userId, userDeletionFuture);
    }

    // 24-hour timer to delete users that didn't log in for 24 hours -- called in GET /me
    public void resetUserDeletionTimer(Integer userId){
        log.warn("Resetting deletion timer for user {} ({} hours)", userId, AppConstants.TIME_FOR_USER_DELETION_HOURS);

        ScheduledFuture<?> userDeletionFuture = pendingUserDeletions.remove(userId);
        if(userDeletionFuture != null){
            userDeletionFuture.cancel(false);
        }

        ScheduledFuture<?> newUserDeletionFuture = scheduler.schedule(()->{
            pendingUserDeletions.remove(userId);
            try{
                userService.handleUserDisconnect(userId);
                log.warn("User {} deleted, didn't re-log after {} hours", userId, AppConstants.TIME_FOR_USER_DELETION_HOURS);
            } catch (Exception e) {
                log.error("Error handling user deletion: {}", e.getMessage());
            }
        }, AppConstants.TIME_FOR_USER_DELETION_HOURS, TimeUnit.HOURS);
        pendingUserDeletions.put(userId, newUserDeletionFuture);
    }

    public void clearAllPendingUserTimers(Integer userId) {
        ScheduledFuture<?> userDeletion = pendingUserDeletions.remove(userId);
        if (userDeletion != null) {
            userDeletion.cancel(false);
        }
        ScheduledFuture<?> roomDisconnect = pendingRoomDisconnects.remove(userId);
        if (roomDisconnect != null) {
            roomDisconnect.cancel(false);
        }
        log.warn("Cleaned up all memory timers for logging out user: {}", userId);
    }
}
