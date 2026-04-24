package com.gabmene.videoguesser.dto.round;

import com.gabmene.videoguesser.entity.UserRound;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserGuessResponseDTO {

    private Integer userId;
    private Long guessedViewCount;
    private Integer scoreEarned;

    public static UserGuessResponseDTO from(UserRound userRound){
        if(userRound == null)
            return null;

        return new UserGuessResponseDTO(
                userRound.getUser().getId(),
                userRound.getLastGuess(),
                userRound.getPointsEarned()
        );
    }
}
