package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    public RoomResponseDTO(Room room) {
        this.id = room.getId();
        this.code = room.getCode();
        this.status = room.getStatus() != null ? room.getStatus().name() : null;
        this.maxPlayers = room.getMaxPlayers();

        this.ownerId = room.getOwner() != null ? room.getOwner().getId() : null;

        // if the room has players, get their nicknames, otherwise get the ownerId's nickname'
        // if no ownerId, return an empty list
        if(room.getUsers() != null && !room.getUsers().isEmpty()) {
            this.players = room.getUsers().stream().map(user -> new PlayerDTO(user.getId(), user.getNickname())).toList();
        } else if(room.getOwner() != null) {
            this.players = List.of(new PlayerDTO(room.getOwner().getId(), room.getOwner().getNickname()));
        } else{
            this.players = List.of();
        }



        this.currentPlayers = this.players.size();
    }
}
