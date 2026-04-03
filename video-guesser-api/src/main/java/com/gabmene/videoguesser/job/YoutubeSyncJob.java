package com.gabmene.videoguesser.job;

import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YoutubeSyncJob {

    private final VideoService videoService;


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
            Category category = new Category();
            category.setName(categoryEnum);
            category.setId(categoryEnum.getId());

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
}
