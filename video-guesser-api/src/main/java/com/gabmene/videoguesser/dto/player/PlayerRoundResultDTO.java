package com.gabmene.videoguesser.dto.player;

import com.gabmene.videoguesser.entity.UserRound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PlayerRoundResultDTO extends PlayerDTO {

    private final Integer pointsScored;
    private final Long lastGuess;

    public PlayerRoundResultDTO(Integer userId, String nickname, Long lastGuess,  Integer pointsScored) {
        super(userId, nickname);
        this.lastGuess = lastGuess;
        this.pointsScored = pointsScored;
    }

    public static PlayerRoundResultDTO from(UserRound userRound){
        return new PlayerRoundResultDTO(
                userRound.getUser().getId(),
                userRound.getUser().getNickname(),
                userRound.getLastGuess(),
                userRound.getPointsEarned()
        );
    }

}