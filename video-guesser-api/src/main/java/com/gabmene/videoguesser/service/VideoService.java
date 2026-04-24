package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.client.YoutubeClient;
import com.gabmene.videoguesser.dto.youtube.YoutubeSearchResponseDTO;
import com.gabmene.videoguesser.dto.youtube.YoutubeVideoDetailsResponseDTO;
import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.entity.Video;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public String fetchAndSaveVideosByCategory(Category category, String pageToken){
        // searching for videos by category
        YoutubeSearchResponseDTO response = youtubeClient.searchVideos("snippet", 50, "video","medium", category.getName().name(), pageToken,apiKey);
        if(response == null || response.getItems() == null || response.getItems().isEmpty()) {
            throw new ResourceNotFoundException("No videos found for category: " + category.getName());
        }

        // transform the response to a list of videos
        List<Video> videosToSave = fetchBaseVideos(response, category, pageToken);

        // remove existing videos with the same youtubeId and videos that are already in the database
        videosToSave = filterUniqueAndNewVideos(videosToSave);

        // if there are no new videos, return the next page token
        if (videosToSave.isEmpty()) {
            return response.getNextPageToken();
        }

        // get the statistics for each video using 'videos' API and merge the view counts with the videos
        enrichVideosWithViewCounts(videosToSave);

        // save the videos to the database
        videoRepository.saveAll(videosToSave);

        return response.getNextPageToken(); // return the next page token if there is one
    }

    private List<Video> filterUniqueAndNewVideos(List<Video> videosToSave) {

        // Transform the list of videos into a map of unique videos
        Map<String, Video> uniqueVideosMap = videosToSave.stream().collect(Collectors
                .toMap(Video::getYoutubeId, video -> video, (v1, v2) -> v1));

        // get the videoIds from the map
        List<String> videoIds = uniqueVideosMap.keySet().stream().toList();

        // get the existing videos from the database using the videoIds
        List<String> existingVideoIdsInDb = videoRepository.findExistingYoutubeIds(videoIds);

        // remove the existing videos from the uniqueVideosMap and return the remaining videos in a list
        return uniqueVideosMap.values().stream().filter(video -> !existingVideoIdsInDb.contains(video.getYoutubeId())).toList();
    }

    private List<Video> fetchBaseVideos(YoutubeSearchResponseDTO response,Category category, String pageToken) {

        return response.getItems().stream().map(item -> {
            Video video = new Video();
            video.setYoutubeId(item.getId().getVideoId());
            video.setTitle(item.getSnippet().getTitle());
            video.setChannelName(item.getSnippet().getChannelTitle());
            video.setCategory(category);
            video.setThumbnailUrl("https://img.youtube.com/vi/" + item.getId().getVideoId() + "/hqdefault.jpg");
            video.setUpdatedAt(LocalDateTime.now());
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
            throw new BusinessException("Error fetching videos");
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



