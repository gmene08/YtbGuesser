package com.gabmene.videoguesser.dto.round;


import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.Video;
import com.gabmene.videoguesser.enums.RoundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActiveRoundResponseDTO {
    private Integer roundId;
    private Integer roundNumber;
    private RoundStatus roundStatus;
    private List<Integer> playersWhoGuessed;
    private ActiveVideoResponseDTO video;
    private Instant endsAt;
    private Instant serverTime;
    private Integer totalGuessingTimeInSeconds;
    private Integer videoStartsAtSecond;

    private RoundResultResponseDTO roundDetails;

    public static ActiveRoundResponseDTO from(Round round) {
        if(round == null)
            return null;

        ActiveVideoResponseDTO video = ActiveVideoResponseDTO.from(round.getVideo());
        List<Integer> guesses = mapGuesses(round);


        // if the round is finished, include the round result -- This is so that the client can display the result when he F5s during the finished round
        RoundResultResponseDTO roundResult = null;
        if (round.getStatus().equals(RoundStatus.FINISHED)) {
            roundResult = RoundResultResponseDTO.from(round);
        }

        return new ActiveRoundResponseDTO(round.getId(),
                round.getRoundNumber(),
                round.getStatus(),
                guesses,
                video, round.getEndsAt(),
                Instant.now(),AppConstants.ROUND_GUESSING_DURATION_SECONDS,
                round.getVideoStartsAtSecond(),
                roundResult );
    }

    private static List<Integer> mapGuesses(Round round) {
        if (round.getUserGuesses() == null || round.getUserGuesses().isEmpty()) {
            return new ArrayList<>();
        }
        return round.getUserGuesses()
                .stream()
                .map(userGuess -> userGuess.getUser().getId())
                .toList();
    }

    @Getter
    @AllArgsConstructor
    public static class ActiveVideoResponseDTO {
        private String url;
        private String thumbnail;

        public static ActiveVideoResponseDTO from(Video video) {
            if(video == null)
                return null;
            return new ActiveVideoResponseDTO( video.getYoutubeId(), video.getThumbnailUrl());
        }
    }

}
