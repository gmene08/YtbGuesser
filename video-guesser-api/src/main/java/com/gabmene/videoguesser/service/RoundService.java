package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.round.CurrentRoundResponseDTO;
import com.gabmene.videoguesser.dto.round.UpdateRoundRequestDTO;
import com.gabmene.videoguesser.entity.*;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.RoundRepository;
import com.gabmene.videoguesser.repository.UserRepository;
import com.gabmene.videoguesser.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final SimpMessagingTemplate messagingTemplate;

    private final RoundRepository roundRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

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

        Round roundUpdated = roundRepository.save(roundToBeUpdated);
        sendRoundUpdate(roundUpdated);

        return roundUpdated;
    }

    private void sendRoundUpdate(Round round) {
        CurrentRoundResponseDTO roundData = CurrentRoundResponseDTO.from(round);
        messagingTemplate.convertAndSend("/topic/game/" + round.getId() + "/round-status", roundData);
    }

}
