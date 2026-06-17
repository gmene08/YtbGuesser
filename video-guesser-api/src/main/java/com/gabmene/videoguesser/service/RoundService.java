package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.dto.round.ActiveRoundResponseDTO;
import com.gabmene.videoguesser.dto.round.RoundResultResponseDTO;
import com.gabmene.videoguesser.dto.round.UpdateRoundRequestDTO;
import com.gabmene.videoguesser.entity.*;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.RoundRepository;
import com.gabmene.videoguesser.repository.UserMatchRepository;
import com.gabmene.videoguesser.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Random;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundService {

    //private final SimpMessagingTemplate messagingTemplate;

    private final RoundRepository roundRepository;
    private final VideoRepository videoRepository;

    //private final GameService gameService;
    //private final GameNotificationService gameNotificationService;

    @Transactional
    public void createRound(Match match, Integer roundNumber){

        Round round = Round.builder()
                .roundNumber(roundNumber)
                .match(match)
                .status(RoundStatus.PREPARING)
                .build();

        // get random video from the categories
        if(match.getCategories() == null || match.getCategories().isEmpty()) {
            throw new BusinessException("No categories selected");
        }
        List<Integer> categoryIds = match.getCategories().stream().map(Category::getId).toList();

        Video video = videoRepository.findRandomVideoByCategories(categoryIds)
                .orElseThrow(() -> new ResourceNotFoundException("No videos found for the selected categories"));

        round.setVideo(video);
        round.setVideoStartsAtSecond(generateRandomStart(round));

        // if the match doesn't have rounds yet, create an empty list'
        if(match.getRounds() == null){
            match.setRounds(new ArrayList<>());
        }

        match.getRounds().add(round);

    }

    public Video getVideoByRoomCode(String roomCode) {
        return roundRepository.findVideoByRoomCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Video not found"));
    }

    @Transactional
    public void updateStatusFromEngine(String roomCode, RoundStatus status){
        Round round = roundRepository.findCurrentRoundByRoomCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Round not found"));
        round.setStatus(status);

        if(status == RoundStatus.GUESSING) {
            round.setEndsAt(Instant.now().plusSeconds(AppConstants.ROUND_GUESSING_DURATION_SECONDS));
        }
        roundRepository.save(round);
        System.out.println("Round status from Room " + roomCode + " updated from engine: " + status);
    }



    private Integer generateRandomStart(Round round){
        Integer roundDuration = AppConstants.ROUND_GUESSING_DURATION_SECONDS;
        Integer videoDuration = round.getVideo().getDurationSeconds() != null ? round.getVideo().getDurationSeconds() : 0;

        int startsAt = 0;

        // only generate a random start if the video duration is greater than the round duration
        if(videoDuration > roundDuration) {
            int maxStart = videoDuration - roundDuration;
            startsAt = new Random().nextInt(maxStart);
        }
        return startsAt;

    }

    public ActiveRoundResponseDTO buildActiveRoundResponseDTO(Round round){
        return ActiveRoundResponseDTO.from(round);
    }

    public RoundResultResponseDTO buildRoundResultResponseDTO(Round round){
        return RoundResultResponseDTO.from(round);
    }

}
