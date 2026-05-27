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

    private final Map<Integer, ScheduledFuture<?>> pendingDisconnects = new ConcurrentHashMap<>();
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if(principal != null){

            Integer userId = Integer.parseInt(principal.getName());
            log.info("Received a new web socket connection: {}", userId);

            ScheduledFuture<?> pendingKick = pendingDisconnects.remove(userId);
            if(pendingKick != null){
                pendingKick.cancel(false);
                log.info("User {} reconnected in time! ", userId);
            } else {
                log.info("User Id {} connected to WebSocket", userId);
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
            log.warn("User Disconnected: {}", userId, ", 60 seconds before disconnecting");

            ScheduledFuture<?> future = scheduler.schedule(()->{
                pendingDisconnects.remove(userId);

                try {
                    userService.handleUserDisconnect(userId);
                    log.info("User {} disconnected", userId);
                } catch (Exception e) {
                    log.error("Error handling user disconnect: {}", e.getMessage());
                }


            },5, TimeUnit.SECONDS);

            pendingDisconnects.put(userId, future);
        }

    }
}
