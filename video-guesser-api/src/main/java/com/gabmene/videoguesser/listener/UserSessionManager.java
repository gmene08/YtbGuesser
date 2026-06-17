/* package com.gabmene.videoguesser.listener;

    ***** UNUSED - KEEPING IT FOR EDUCATIONAL PURPOSES *****

    - This was made before migrating the WebSocket stuff fully to the node engine

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.event.UserLeftRoomEvent;
import com.gabmene.videoguesser.listener.UserConnectionRegistry;
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

    private final UserConnectionRegistry userConnectionRegistry;
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if(principal != null){

            Integer userId = Integer.parseInt(principal.getName());
            log.warn("Received a new web socket connection: user {}", userId);

            if(this.clearUserDeletionTimer(userId)){
                log.warn("User {} connected to WebSocket - deletion timer cleared", userId);
            }
        }
    }

    @EventListener
    public void handleUserSubscribeToRoomEvent(SessionSubscribeEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        String destination = accessor.getDestination();

        // Check the room subscription
        if(principal != null && (destination != null && destination.startsWith("/topic/room/"))){
            Integer userId = Integer.parseInt(principal.getName());
            String subRoomCode = destination.split("/")[3];

            // Check the room the user is in
            roomRepository.findRoomCodeByUserId(userId).ifPresentOrElse(userRoomCode -> {


                    // check if the room the user is trying to subscribe to is the same as the room the user is in
                    if(subRoomCode.equals(userRoomCode)){
                        userConnectionRegistry.markAsConnected(userId, true); // mark the user as connected
                        if (this.clearRoomDisconnectTimer(userId)){
                            log.warn("✅ User {} reconnected to room {} in time!", userId , subRoomCode);
                        }

                    } else {
                        log.warn("User {} tried to subscribe to room {} but is not in it, belongs to {}. Forcing disconnect", userId, subRoomCode, userRoomCode);
                        this.clearRoomDisconnectTimer(userId);
                        try {
                            roomService.handleRoomDisconnect(userId);
                            log.warn("User {} kicked from old room", userId);
                        } catch (Exception e) {
                            log.error("Error handling user disconnect: {}", e.getMessage());
                        }
                    }
                },()-> log.warn("User {} tried to subscribe to room {} but has no room assigned in DB.", userId, subRoomCode));
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if(principal != null){

            Integer userId = Integer.parseInt(principal.getName());
            log.warn("Received a WebSocket disconnect event: user {}", userId);

            // if the user didn't leave the room via button, only closed the tab, the user would still be in the room.
            roomRepository.findRoomCodeByUserId(userId).ifPresent(roomCode->{
                log.warn("Lost Connection - User {} lost connectin to room {}, {} {} for rejoining", userId, roomCode, AppConstants.TIME_FOR_ROOM_DISCONNECT_SECONDS, TimeUnit.SECONDS);

                // Logic to alert the frontend that the user has disconnected.
                userConnectionRegistry.markAsDisconnected(userId, true);

                // Schedule a timer to kick the user from the room.
                this.scheduleRoomDisconnect(
                        userId,
                        roomCode,
                        AppConstants.TIME_FOR_ROOM_DISCONNECT_SECONDS,
                        TimeUnit.SECONDS,
                        "Disconnected - User {} disconnected from room {} for not rejoining in {} {}");

                // If he left the room via button, this timer should be skipped since the user would be redirected to the home page.
                // Which means this timer would be called already through /me.
                this.resetUserDeletionTimer(userId);
            });
        }
    }

    @EventListener
    public void handleUserLeftRoomEvent(UserLeftRoomEvent event){
        Integer userId = event.userId();
        if(this.clearRoomDisconnectTimer(userId))
            log.warn("Timer canceled - User {} left the room (or was kicked) before the disconnect timer expired.", userId);
    }

    // 24-hour timer to delete users that didn't log in for 24 hours -- called in GET /me
    public void resetUserDeletionTimer(Integer userId){
        if(this.clearUserDeletionTimer(userId)){
            log.warn("Deletion Timer Reset - User {} deletion in {} {} ", userId, AppConstants.TIME_FOR_USER_DELETION_HOURS, TimeUnit.HOURS);
        }

        this.scheduleUserDeletion(
                userId,
                AppConstants.TIME_FOR_USER_DELETION_HOURS,
                TimeUnit.HOURS,
                "User {} Deleted - Not logging in for {} {}"
        );
    }

    // If guest, after its creation, doesn't connect to WebSocket in 10 minutes, delete him -- called in POST /guest
    public void scheduleInitialDestruction(Integer userId) {
        log.warn("Initial Destruction Scheduling - User {} deletion in {} {} if not connected to WebSocket until then", userId, AppConstants.TIME_BEFORE_FIRST_GUEST_DELETION_MINUTES, TimeUnit.MINUTES);
        this.scheduleUserDeletion(
                userId,
                AppConstants.TIME_BEFORE_FIRST_GUEST_DELETION_MINUTES,
                TimeUnit.MINUTES,
                "User {} Deleted - Not connecting to WebSocket for a first time in {} {}"
        );
    }

    public void scheduleRoomDisconnect(Integer userId, String roomCode, int timeForRoomDisconnect, TimeUnit timeUnit, String message) {
        ScheduledFuture<?> roomDisconnectFuture = scheduler.schedule(()->{
            pendingRoomDisconnects.remove(userId);

            // not sending update to frontend because the user is supposed to be disconnected, not reconnected.
            // If sent, the player-card would show the user as connected for a short time before being kicked.
            userConnectionRegistry.markAsConnected(userId, false);

            try {
                roomService.handleRoomDisconnect(userId);
                log.warn(message, userId, roomCode, timeForRoomDisconnect, timeUnit);
            } catch (Exception e) {
                log.error("Error handling user disconnect from room: {}", e.getMessage());
            }

        }, timeForRoomDisconnect, timeUnit);

        pendingRoomDisconnects.put(userId, roomDisconnectFuture);
    }

    public void scheduleUserDeletion(Integer userId, int timeForDeletion, TimeUnit timeUnit, String message) {
        ScheduledFuture<?> newUserDeletionFuture = scheduler.schedule(()->{
            pendingUserDeletions.remove(userId);
            try{
                userService.handleUserDisconnect(userId);
                log.warn(message, userId, timeForDeletion, timeUnit);
            } catch (Exception e) {
                log.error("Error handling user deletion: {}", e.getMessage());
            }
        }, timeForDeletion, timeUnit);

        pendingUserDeletions.put(userId, newUserDeletionFuture);
    }

    public boolean clearRoomDisconnectTimer(Integer userId) {
        ScheduledFuture<?> roomDisconnect = pendingRoomDisconnects.remove(userId);
        if (roomDisconnect != null) {
            roomDisconnect.cancel(false);
            return true;
        }
        return false;
    }

    public boolean clearUserDeletionTimer(Integer userId) {
        ScheduledFuture<?> userDeletion = pendingUserDeletions.remove(userId);
        if (userDeletion != null) {
            userDeletion.cancel(false);
            return true;
        }
        return false;
    }

    public void clearAllPendingUserTimers(Integer userId) {
        clearRoomDisconnectTimer(userId);
        clearUserDeletionTimer(userId);
        log.warn("Cleaned up all memory timers for logging out user: {}", userId);
    }
} */
