package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.Video;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.repository.RoundRepository;
import com.gabmene.videoguesser.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public void createRound(Match match, Integer roundNumber){

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

        if(match.getRounds() == null){
            match.setRounds(new ArrayList<>());
        }
        match.getRounds().add(round);

    }

    public Video getVideoByRoomCode(String roomCode) {
        return roundRepository.findVideoByRoomCode(roomCode).orElseThrow(()-> new RuntimeException("Video not found"));
    }
}
