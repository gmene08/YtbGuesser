package com.gabmene.videoguesser.job;

import com.gabmene.videoguesser.repository.UserRepository;
import com.gabmene.videoguesser.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketDisconnectListener {

    private final UserRepository userRepository;
    private final RoomService roomService;

    @EventListener
    public void handleWebScoketDisconnectListener(SessionDisconnectEvent event){

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if(principal != null){
            Integer userId = Integer.parseInt(principal.getName());
            userRepository.findById(userId).ifPresent(user -> {
                if(user.getRoom() != null){
                    log.info("User {} disconnected from room {}", user.getId(), user.getRoom().getCode());
                    try {
                        roomService.leaveRoom(user.getRoom().getCode(), user.getId());
                    } catch (Exception e) {
                        log.error("Error leaving room: {}", e.getMessage());
                    }
                }
            });
        }
    }
}
