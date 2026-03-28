package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.enums.MatchCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartRoomRequestDTO {
    private Integer userId; // ownerId that is trying to start the room

    // Match configurations
    private Integer numberOfRounds;
    private List<MatchCategory> category;
}
