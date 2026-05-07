package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.dto.round.ActiveRoundResponseDTO;
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

import java.util.Map;
import java.util.Random;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final SimpMessagingTemplate messagingTemplate;

    private final RoundRepository roundRepository;
    private final VideoRepository videoRepository;
    private final UserMatchRepository userMatchRepository;

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

    public Round updateRound(Integer roundId, UpdateRoundRequestDTO request) {
        Round roundToBeUpdated = roundRepository.findById(roundId).orElseThrow(()->new ResourceNotFoundException("Round not found"));

        if(!request.getUserId().equals(roundToBeUpdated.getMatch().getRoom().getOwner().getId())) {
            throw new BusinessException("User is not the owner of the room");
        }

        roundToBeUpdated.setStatus(request.getStatus());

        // if the round is in guessing state, set the end time to 30 seconds from now
        if(request.getStatus() == RoundStatus.GUESSING) {
            roundToBeUpdated.setEndsAt(Instant.now().plusSeconds(30));
        }

        if(request.getStatus() == RoundStatus.FINISHED) {
            processRoundResults(roundToBeUpdated);
        }

        Round roundUpdated = roundRepository.save(roundToBeUpdated);
        sendRoundUpdate(roundUpdated);

        return roundUpdated;
    }
    private void processRoundResults(Round round) {
        Map<Integer,Integer> userCurrentTotalPoints = round.getMatch().getUserMatches().stream().collect(Collectors.toMap(
                userMatch -> userMatch.getUser().getId(),
                UserMatch::getCurrentScore
        ));

        List<UserMatch> userMatchesToBeUpdated = new ArrayList<>();

        for(UserRound userGuess : round.getUserGuesses()) {
            Integer currentTotalPoints = userCurrentTotalPoints.getOrDefault(userGuess.getUser().getId(), 0);
            Integer pointsScored = userGuess.getPointsEarned();
            Integer newTotalPoints = currentTotalPoints + pointsScored;

            UserMatch userMatchToBeUpdated = userMatchRepository.findByUserIdAndMatchId(userGuess.getUser().getId(), round.getMatch().getId()).orElse(
                    new UserMatch().builder()
                            .user(userGuess.getUser())
                            .match(round.getMatch())
                            .finalScore(0)
                            .currentScore(0)
                            .build()
            );

            userMatchToBeUpdated.setCurrentScore(newTotalPoints);
            userMatchesToBeUpdated.add(userMatchToBeUpdated);
        }
        userMatchRepository.saveAll(userMatchesToBeUpdated);
    }

    private void sendRoundUpdate(Round round) {
        ActiveRoundResponseDTO roundData = ActiveRoundResponseDTO.from(round);
        messagingTemplate.convertAndSend("/topic/game/" + round.getId() + "/round-status", roundData);
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

}
