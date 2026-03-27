package com.gabmene.videoguesser.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartRoomRequestDTO {
    private Integer userId; // ownerId that is trying to start the room
}
