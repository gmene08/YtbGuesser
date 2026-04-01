package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.client.YoutubeClient;
import com.gabmene.videoguesser.dto.youtube.YoutubeSearchResponseDTO;
import com.gabmene.videoguesser.dto.youtube.YoutubeVideoDetailsResponseDTO;
import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.entity.Video;
import com.gabmene.videoguesser.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final YoutubeClient youtubeClient;
    private final VideoRepository videoRepository;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Transactional
    public void fetchAndSaveVideosByCategory(Category category){

        // search for videos using 'search' API
        List<Video> videosToSave = fetchBaseVideos(category);

        // get the statistics for each video using 'videos' API and merge the view counts with the videos
        enrichVideosWithViewCounts(videosToSave);

        // save the videos to the database
        videoRepository.saveAll(videosToSave);
    }

    private List<Video> fetchBaseVideos(Category category) {
        // searching for videos by category
        YoutubeSearchResponseDTO response = youtubeClient.searchVideos("snippet", 50, "video", category.getName().name(), apiKey);
        if(response == null || response.getItems() == null || response.getItems().isEmpty()) {
            throw new RuntimeException("No videos found for category: " + category.getName());
        }

        // transform the response to a list of videos
        return response.getItems().stream().map(item -> {
            Video video = new Video();
            video.setYoutubeId(item.getId().getVideoId());
            video.setTitle(item.getSnippet().getTitle());
            video.setChannelName(item.getSnippet().getChannelTitle());
            video.setCategory(category);
            video.setThumbnailUrl("https://img.youtube.com/vi/" + item.getId().getVideoId() + "/hqdefault.jpg");
            return video;
        }).toList();
    }

    private void enrichVideosWithViewCounts(List<Video> videosToSave) {
        // get the video details for the view count
        List<String> videoIds = videosToSave.stream().map(Video::getYoutubeId).toList();
        String videoIdsString = String.join(",", videoIds);
        YoutubeVideoDetailsResponseDTO detailsResponse = youtubeClient.getVideosDetails("statistics", videoIdsString, apiKey);

        // transform the response to a list of view counts
        if(detailsResponse == null || detailsResponse.getItems() == null || detailsResponse.getItems().isEmpty()){
            throw new RuntimeException("Error fetching videos");
        }
        Map<String, Long> viewCounts = detailsResponse.getItems().stream()
                .filter(item -> item.getId() != null) // ignore videos without an ID
                .collect(Collectors.toMap(
                item -> item.getId(),
                item -> item.getStatistics().getViewCount(),
                (view1, view2) -> view1 // if ID is already present, keep the existing value
                ));

        // merge the videos and view counts
        for (Video video : videosToSave) {
            video.setViewCount(viewCounts.getOrDefault(video.getYoutubeId(), 0L));
        }
    }
}



