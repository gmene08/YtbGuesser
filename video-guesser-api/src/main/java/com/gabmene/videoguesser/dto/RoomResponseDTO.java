package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private List<String> users;
    private String owner;
    private Integer currentPlayers;

    public RoomResponseDTO(Room room) {
        this.id = room.getId();
        this.code = room.getCode();
        this.status = this.status = room.getStatus() != null ? room.getStatus().name() : null;;
        this.maxPlayers = room.getMaxPlayers();
        this.users = (room.getUsers() != null)
                ? room.getUsers().stream().map(User::getNickname).toList()
                : List.of(room.getOwner().getNickname()); // Owner is always in the room
        this.owner = room.getOwner().getNickname();
        this.currentPlayers = !room.getUsers().isEmpty() ? room.getUsers().size() : 0;
    }
}
