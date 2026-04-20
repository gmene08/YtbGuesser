package com.gabmene.videoguesser.dto;

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

    @Getter
    @AllArgsConstructor
    public static class PlayerDTO {
        private Integer id;
        private String nickname;
    }

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
        if (room.getUsers() != null && !room.getUsers().isEmpty()) {
            return room.getUsers().stream()
                    .map(user -> new PlayerDTO(user.getId(), user.getNickname()))
                    .toList();
        }

        if (room.getOwner() != null) {
            return List.of(new PlayerDTO(room.getOwner().getId(), room.getOwner().getNickname()));
        }

        return List.of();
    }
}
