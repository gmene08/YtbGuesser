package com.gabmene.videoguesser.dto.round;


import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.UserRound;
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
public class CurrentRoundResponseDTO {
    private Integer roundId;
    private Integer roundNumber;
    private RoundStatus roundStatus;
    private List<Integer> playersWhoGuessed;
    private VideoResponseDTO video;
    private Instant endsAt;

    public static CurrentRoundResponseDTO from(Round round) {
        if(round == null)
            return null;

        VideoResponseDTO video = VideoResponseDTO.from(round.getVideo());
        List<Integer> guesses = mapGuesses(round);

        return new CurrentRoundResponseDTO(round.getId(), round.getRoundNumber(), round.getStatus(), guesses, video, round.getEndsAt() );
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
    public static class VideoResponseDTO {
        private String channelName;
        private String url;
        private String thumbnail;
        private String title;
        private Long viewCount;

        public static VideoResponseDTO from(Video video) {
            if(video == null)
                return null;
            return new VideoResponseDTO(video.getChannelName(), video.getYoutubeId(), video.getThumbnailUrl(), video.getTitle(), video.getViewCount());
        }
    }

}
