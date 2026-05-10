package com.gabmene.videoguesser.dto.room;

import com.gabmene.videoguesser.dto.player.PlayerDTO;
import com.gabmene.videoguesser.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RoomResponseDTO {

    private Integer id;
    private String code;
    private String status;
    private Integer maxPlayers;
    private List<PlayerDTO> players;
    private Integer ownerId;
    private Integer currentPlayers;


    public static RoomResponseDTO from(Room room) {
        List<PlayerDTO> players = mapPlayers(room);

        return new RoomResponseDTO(
                room.getId(),
                room.getCode(),
                room.getStatus() != null ? room.getStatus().name() : null,
                room.getMaxPlayers(),
                players,
                room.getOwner() != null ? room.getOwner().getId() : null,
                players.size()
        );
    }

    private static List<PlayerDTO> mapPlayers(Room room) {
        // check if the room has users - If it does, return the list of users
        if (room.getUsers() != null && !room.getUsers().isEmpty()) {
            return room.getUsers().stream()
                    .map(user -> new PlayerDTO(user.getId(), user.getNickname()))
                    .toList();
        }

        // if the room has no users, check if it has an owner - If it does, return the owner
        if (room.getOwner() != null) {
            return List.of(new PlayerDTO(room.getOwner().getId(), room.getOwner().getNickname()));
        }

        // if the room has no users and no owner, return an empty list
        return List.of();
    }
}
