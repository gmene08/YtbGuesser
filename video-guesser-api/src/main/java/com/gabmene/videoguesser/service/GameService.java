package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.round.UserGuessRequestDTO;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.entity.UserRound;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ConflictException;
import com.gabmene.videoguesser.exception.ForbiddenException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.RoundRepository;
import com.gabmene.videoguesser.repository.UserRepository;
import com.gabmene.videoguesser.repository.UserRoundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {
    private final SimpMessagingTemplate messagingTemplate;

    private final RoundRepository roundRepository;
    private final UserRepository userRepository;
    private final UserRoundRepository userRoundRepository;

    @Transactional
    public UserRound processGuess(Integer roundId, UserGuessRequestDTO userGuessRequest){

        Round round = roundRepository.findById(roundId).orElseThrow(()-> new ResourceNotFoundException("Round not found"));
        User user = userRepository.findById(userGuessRequest.getUserId()).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if(round.getMatch() == null){
            throw new ResourceNotFoundException("Match from round:" + roundId + " not found");
        }
        Room room = round.getMatch().getRoom();

        if(room == null){
            throw new ResourceNotFoundException("room from match:" + round.getMatch().getId() + " not found");
        }

        if(round.getStatus() != RoundStatus.GUESSING ) {
            throw new BusinessException("Round is not in guessing state");
        }

        if( user.getRoom() == null || !user.getRoom().equals(room)) {
            throw new ForbiddenException("User is not in the room");
        }

        // check if the user has already guessed in this round
        if (userRoundRepository.existsByUserIdAndRoundId(user.getId(), round.getId())) {
            throw new ConflictException("User already guessed in this round");
        }

        Long viewCount = round.getVideo().getViewCount();
        Long userGuess = userGuessRequest.getGuessedViewCount();

        Integer points = GameService.calculatePointsEarned(viewCount, userGuess);
        UserRound userRound;
        userRound = UserRound.builder()
                .user(user)
                .round(round)
                .lastGuess(userGuess)
                .pointsEarned(points).build();
        userRoundRepository.save(userRound);
        System.out.println("User " + user.getId() + " guessed " + userGuess + " in round " + round.getId() + " and earned " + points + " points");
        return userRound;
    }

    public static Integer calculatePointsEarned(Long viewCount, Long userGuess){

        int maxPoints = 1000;
        double tolerance = 3.0;

        // get the logarithm of the view count and the user's guess
        double logViewCount = Math.log10(viewCount + 1); // add 1 to the view count and guess to avoid division by zero
        double logGuess =  Math.log10(userGuess + 1);

        // subtract the logarithm of the view count and the user's guess
        double difference = Math.abs(logViewCount - logGuess);

        // divide by the tolerance to get the difference
        // multiply by the maximum points
        double points = (double)maxPoints * (1 - (difference / tolerance));

        // If the score is negative, set it to 0
        return Math.max(0, (int) points);

    }
}
