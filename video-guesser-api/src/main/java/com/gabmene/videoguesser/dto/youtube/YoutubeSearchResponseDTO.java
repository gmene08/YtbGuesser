package com.gabmene.videoguesser.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeSearchResponseDTO {
    private String nextPageToken;
    private List<YoutubeItemDTO> items;
}
