package com.gabmene.videoguesser.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeSearchResponseDTO {
    private String nextPageToken;
    private List<Item> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Id id;
        private Snippet snippet;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Id {
        private String videoId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        private String title;
        private String channelTitle;
    }
}
