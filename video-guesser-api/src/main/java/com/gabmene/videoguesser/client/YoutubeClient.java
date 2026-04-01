package com.gabmene.videoguesser.client;

import com.gabmene.videoguesser.dto.youtube.YoutubeSearchResponseDTO;
import com.gabmene.videoguesser.dto.youtube.YoutubeVideoDetailsResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "youtubeClient", url = "https://www.googleapis.com/youtube/v3")
public interface YoutubeClient {

    @GetMapping("/search")
    YoutubeSearchResponseDTO searchVideos(
            @RequestParam("part") String part,
            @RequestParam("maxResults")int maxResults,
            @RequestParam("type") String type,
            @RequestParam("q") String query,
            @RequestParam("key") String apiKey
    );

    @GetMapping("/videos")
    YoutubeVideoDetailsResponseDTO getVideosDetails(
            @RequestParam("part") String part,
            @RequestParam("id") String videoIds,
            @RequestParam("key") String apiKey
    );
}
