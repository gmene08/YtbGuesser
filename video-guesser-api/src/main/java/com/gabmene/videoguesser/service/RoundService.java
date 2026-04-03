package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.Video;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.repository.RoundRepository;
import com.gabmene.videoguesser.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public Round createRound(Match match, Integer roundNumber){
        Round round = Round.builder()
                .roundNumber(roundNumber)
                .match(match)
                .status(RoundStatus.PREPARING)
                .build();

        // get random video from the categories
        List<Integer> categoryIds = match.getCategories().stream().map(Category::getId).toList();

        Video video = videoRepository.findRandomVideoByCategories(categoryIds)
                .orElseThrow(() -> new RuntimeException("No videos found for the selected categories"));

        round.setVideo(video);

        return roundRepository.save(round);
    }
}
