package com.gabmene.videoguesser.dto.player;

import com.gabmene.videoguesser.entity.UserMatch;
import lombok.Getter;

@Getter
public class PlayerCurrentScoreDTO extends PlayerDTO {

    private final Integer totalScore;

    public PlayerCurrentScoreDTO(Integer userId, String nickname, Integer totalScore) {
        super(userId, nickname);
        this.totalScore = totalScore;
    }

    public static PlayerCurrentScoreDTO from(UserMatch userMatch){
        return new PlayerCurrentScoreDTO(userMatch.getUser().getId(), userMatch.getUser().getNickname(), userMatch.getCurrentScore());
    }
}
