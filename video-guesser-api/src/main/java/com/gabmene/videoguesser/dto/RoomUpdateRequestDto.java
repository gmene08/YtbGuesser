package com.gabmene.videoguesser.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomUpdateRequestDto {
    @NotNull (message = "Max players cannot be null")
    @Min(value = 1, message = "Max players must be greater than 0")
    @Max(value = 10, message = "Max players must be at most 10")
    private Integer maxPlayers;
}
