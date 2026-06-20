package com.gabmene.videoguesser.job;

import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.entity.Video;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.repository.VideoRepository;
import com.gabmene.videoguesser.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor

public class YoutubeSyncJob {

    private final VideoService videoService;
    private final VideoRepository videoRepository;

    @Scheduled(initialDelay = 43200000,fixedRate = 43200000)
    public void syncVideosJob() {

        System.out.println("Initializing Youtube Sync Job");

        // fetch videos for each category
        for (MatchCategory categoryEnum : MatchCategory.values()) {
            System.out.println("Getting videos from category: " + categoryEnum.name());

            // skip the 'ALL' category - Don't want to save the videos with 'ALL' as a category in the database
            if (categoryEnum == MatchCategory.ALL) {
                continue;
            }

            // create a new category object based on the enum value
            Category category = new Category(categoryEnum.getId(), categoryEnum, categoryEnum.name().toLowerCase());

            String nextPageToken = null; // starts without a token (page 1)
            int pagesToFetch = 3;

            for(int i = 0; i < pagesToFetch; i++){
                try {
                    nextPageToken = videoService.fetchAndSaveVideosByCategory(category, nextPageToken);

                    if (nextPageToken == null) {
                        break; // if it returns null, it means there are no more pages to fetch
                    }
                }catch (Exception e){
                    System.out.println("Error fetching videos for category: " + categoryEnum.name() + " at page: " + i
                            + " - " + e.getMessage());
                }
            }
        }
        System.out.println("Youtube Sync Job completed");

    }

    @Scheduled(initialDelay = 86400000,fixedRate = 86400000)
    public void updateVideosViewCount(){

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Video> videosToUpdate = videoRepository.findTop50ByUpdatedAtBeforeOrderByUpdatedAtAsc(sevenDaysAgo);
        while (!videosToUpdate.isEmpty()){
            try {
                videoService.updateOldVideoViews(videosToUpdate);
                videosToUpdate = videoRepository.findTop50ByUpdatedAtBeforeOrderByUpdatedAtAsc(sevenDaysAgo);
            } catch (Exception e) {
                System.out.println("Error updating videos: " + e.getMessage());
            }
        }

        System.out.println("Daily view count update completed");
    }
}
