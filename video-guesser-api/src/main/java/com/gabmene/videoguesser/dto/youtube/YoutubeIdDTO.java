package com.gabmene.videoguesser.dto.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeIdDTO {
    private String videoId;
}