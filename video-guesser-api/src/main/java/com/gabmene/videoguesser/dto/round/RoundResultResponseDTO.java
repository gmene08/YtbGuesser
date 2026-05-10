package com.gabmene.videoguesser.dto.round;

import com.gabmene.videoguesser.dto.player.PlayerRoundResultDTO;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.UserRound;
import com.gabmene.videoguesser.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RoundResultResponseDTO {

    private List<PlayerRoundResultDTO> playersScore;
    private VideoResultDTO videoDetails;

    public static RoundResultResponseDTO from(Round round){
        return new RoundResultResponseDTO(
                mapPlayers(round.getUserGuesses()),
                round.getVideo() != null ? VideoResultDTO.from(round.getVideo()) : null
        );
    }

    private static List<PlayerRoundResultDTO> mapPlayers(List<UserRound> userRounds){
        if(userRounds == null || userRounds.isEmpty()) {
            return null;
        }
        return userRounds.stream().map(PlayerRoundResultDTO::from).toList();
    }



    @Getter
    @AllArgsConstructor
    public static class VideoResultDTO {

        private String url;
        private String thumbnail;
        private String title;
        private String channelTitle;
        private Long viewCount;

        public static VideoResultDTO from(Video video){
            return new VideoResultDTO(video.getYoutubeId(), video.getThumbnailUrl(), video.getTitle(), video.getChannelName(), video.getViewCount());
        }

    }
}
