package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoomResponseDTO {

    private Integer id;
    private String code;
    private String status;
    private Integer maxPlayers;
    private List<String> users;
    private String owner;

    public RoomResponseDTO(Room room) {
        this.id = room.getId();
        this.code = room.getCode();
        this.status = room.getStatus().toString();
        this.maxPlayers = room.getMaxPlayers();
        this.users = (room.getUsers() != null)
                ? room.getUsers().stream().map(User::getNickname).toList()
                : List.of(room.getOwner().getNickname()); // Owner is always in the room
        this.owner = room.getOwner().getNickname();
    }
}
