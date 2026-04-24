package com.gabmene.videoguesser.dto.round;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGuessRequestDTO {

    @NotNull(message = "User id cannot be null")
    private Integer userId;

    @NotNull(message = "Guess cannot be null")
    @Min(value = 1, message = "Guess must be greater than 0")
    private Long guessedViewCount;

}
