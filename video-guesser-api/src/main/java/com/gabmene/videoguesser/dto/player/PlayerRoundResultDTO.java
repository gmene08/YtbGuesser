package com.gabmene.videoguesser.dto.player;

import com.gabmene.videoguesser.entity.UserRound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PlayerRoundResultDTO extends PlayerDTO {

    private final Integer pointsScored;

    public PlayerRoundResultDTO(Integer userId, String nickname, Integer pointsScored) {
        super(userId, nickname);
        this.pointsScored = pointsScored;
    }

    public static PlayerRoundResultDTO from(UserRound userRound){
        return new PlayerRoundResultDTO(
                userRound.getUser().getId(),
                userRound.getUser().getNickname(),
                userRound.getPointsEarned()
        );
    }

}