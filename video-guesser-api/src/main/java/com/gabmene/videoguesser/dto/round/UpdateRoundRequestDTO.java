package com.gabmene.videoguesser.dto.round;

import com.gabmene.videoguesser.enums.RoundStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class UpdateRoundRequestDTO {

    @NotNull(message = "User id cannot be null")
    private Integer userId;

    @NotNull(message = "Round status cannot be null")
    private RoundStatus status;




}
