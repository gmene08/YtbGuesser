package com.gabmene.videoguesser.dto.round;


import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.Video;
import com.gabmene.videoguesser.enums.RoundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentRoundResponseDTO {
    private Integer roundId;
    private Integer roundNumber;
    private RoundStatus roundStatus;
    private VideoResponseDTO video;

    public static CurrentRoundResponseDTO from(Round round) {
        if(round == null)
            return null;

        VideoResponseDTO video = VideoResponseDTO.from(round.getVideo());

        return new CurrentRoundResponseDTO(round.getId(), round.getRoundNumber(), round.getStatus(), video );
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
