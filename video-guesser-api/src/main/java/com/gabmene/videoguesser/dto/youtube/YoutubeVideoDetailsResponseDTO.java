package com.gabmene.videoguesser.dto.youtube;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeVideoDetailsResponseDTO {
    private List<VideoDetailItemDTO> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoDetailItemDTO {
        private String id;
        private VideoStatisticsDTO statistics;
        private ContentDetailsDTO contentDetails;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoStatisticsDTO {
        private Long viewCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentDetailsDTO {
        private String duration;
        private ContentRatingDTO contentRating;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentRatingDTO {
        private String ytRating;
    }
}
