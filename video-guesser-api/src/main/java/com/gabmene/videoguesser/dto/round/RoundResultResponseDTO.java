package com.gabmene.videoguesser.dto.round;

import com.gabmene.videoguesser.entity.UserRound;
import com.gabmene.videoguesser.entity.Video;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RoundResultResponseDTO {

    private List<PlayerResultDTO> players;
    private VideoResultDTO video;

    public static RoundResultResponseDTO from(List<UserRound> userRounds){
        return new RoundResultResponseDTO(
                mapPlayers(userRounds),
                userRounds.get(0).getRound().getVideo() != null ? VideoResultDTO.from(userRounds.get(0).getRound().getVideo()) : null
        );
    }

    private static List<PlayerResultDTO> mapPlayers(List<UserRound> userRounds){
        if(userRounds == null || userRounds.isEmpty()) {
            return null;
        }
        return userRounds.stream().map(PlayerResultDTO::from).toList();
    }

    @Getter
    @AllArgsConstructor
    public static class PlayerResultDTO {

        private Integer userId;
        private Integer score;

        public static PlayerResultDTO from(UserRound userRound){
            return new PlayerResultDTO(userRound.getUser().getId(), userRound.getPointsEarned());
        }

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
