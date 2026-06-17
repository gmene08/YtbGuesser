/*package com.gabmene.videoguesser.listener;

   // ***** Unused, keeping it for educational purposes *****

import com.gabmene.videoguesser.dto.room.RoomResponseDTO;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.repository.RoomRepository;
import com.gabmene.videoguesser.service.GameNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class UserConnectionRegistry {
    private final GameNotificationService gameNotificationService;
    private final RoomRepository roomRepository;

    private final Set<Integer> disconnectedUsers = ConcurrentHashMap.newKeySet();

    @Transactional
    public void markAsDisconnected(Integer userId, boolean sendUpdate) {
        disconnectedUsers.add(userId);
        if(sendUpdate) this.sendDisconnectedUsersUpdate(userId);
    }

    @Transactional
    public void markAsConnected(Integer userId, boolean sendUpdate) {
        disconnectedUsers.remove(userId);
        if(sendUpdate) this.sendDisconnectedUsersUpdate(userId);
    }

    public boolean isDisconnected(Integer userId) {
        return disconnectedUsers.contains(userId);
    }

    public List<Integer> getDisconnectedUsersInRoom(Room room) {
        return room.getUsers().stream().map(User::getId).filter(disconnectedUsers::contains).toList();
    }

    public void sendDisconnectedUsersUpdate(Integer userId){
        roomRepository.findRoomByUserId(userId).ifPresent(room -> {
            // Get the list of users that were disconnected in the room.
            List<Integer> disconnectedInRoom = room.getUsers().stream().map(User::getId).filter(disconnectedUsers::contains).toList();
            //gameNotificationService.sendRoomUpdate(RoomResponseDTO.from(room, disconnectedInRoom));
        });
    }
}
*/