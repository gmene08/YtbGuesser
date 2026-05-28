package com.gabmene.videoguesser.listener;

import com.gabmene.videoguesser.repository.UserRepository;
import com.gabmene.videoguesser.service.RoomService;
import com.gabmene.videoguesser.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final UserRepository userRepository;
    private final RoomService roomService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Map<Integer, ScheduledFuture<?>> pendingRoomDisconnects = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledFuture<?>> pendingUserDeletions = new ConcurrentHashMap<>();
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if(principal != null){

            Integer userId = Integer.parseInt(principal.getName());
            log.info("Received a new web socket connection: {}", userId);

            ScheduledFuture<?> pendingRoomDisconnect = pendingRoomDisconnects.remove(userId);
            ScheduledFuture<?> pendingUserDeletion = pendingUserDeletions.remove(userId);

            boolean wasRescued = false;

            if(pendingRoomDisconnect != null){
                pendingRoomDisconnect.cancel(false);
                wasRescued = true;
            }
            if(pendingUserDeletion != null){
                pendingUserDeletion.cancel(false);
                wasRescued = true;
            }
            if (wasRescued) {
                log.info("✅ User {} reconnected in time!", userId);
            } else {
                log.info("🔌 User {} connected to WebSocket for the first time!", userId);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("Received a disconnect event");
        StompHeaderAccessor acessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = acessor.getUser();
        if(principal != null){
            Integer userId = Integer.parseInt(principal.getName());
            log.warn("User Disconnected: {}, 60 seconds before disconnecting", userId );

            ScheduledFuture<?> roomDisconnectFuture = scheduler.schedule(()->{
                pendingRoomDisconnects.remove(userId);

                try {
                    roomService.handleRoomDisconnect(userId);
                    log.info("User {} disconnected", userId);
                } catch (Exception e) {
                    log.error("Error handling user disconnect: {}", e.getMessage());
                }

            },120, TimeUnit.SECONDS);

            ScheduledFuture<?> userDeletionFuture = scheduler.schedule(()->{
                pendingUserDeletions.remove(userId);

                try {
                    userService.handleUserDisconnect(userId);
                    log.info("User {} deleted", userId);
                } catch (Exception e) {
                    log.error("Error handling user deletion: {}", e.getMessage());
                }
            }, 86400, TimeUnit.SECONDS);

            pendingRoomDisconnects.put(userId, roomDisconnectFuture);
            pendingUserDeletions.put(userId, userDeletionFuture);
        }

    }
}
