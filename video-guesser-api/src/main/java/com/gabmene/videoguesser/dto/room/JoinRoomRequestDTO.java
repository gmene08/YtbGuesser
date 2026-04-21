package com.gabmene.videoguesser.dto.room;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequestDTO {
    @NotNull (message = "User id cannot be null")
    private Integer userId;


}
