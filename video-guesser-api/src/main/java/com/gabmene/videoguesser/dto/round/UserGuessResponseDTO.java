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
    private Boolean hasGuessed;

    public static UserGuessResponseDTO from(UserRound userRound, Boolean hasGuessed){
        return new UserGuessResponseDTO(userRound.getUser().getId(), hasGuessed);
    }
}
