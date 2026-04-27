package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.round.UserGuessRequestDTO;
import com.gabmene.videoguesser.dto.round.UserGuessResponseDTO;
import com.gabmene.videoguesser.entity.UserRound;
import com.gabmene.videoguesser.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class GameWebSocketController {
    private final GameService gameService;

    @MessageMapping("/game/{roundId}/guess")
    @SendTo("/topic/game/{roundId}/guessed-status")
    public UserGuessResponseDTO guess(@DestinationVariable Integer roundId, @Valid @Payload UserGuessRequestDTO userGuess){
        UserRound userRound = gameService.processGuess(roundId, userGuess);
        return UserGuessResponseDTO.from(userRound, true);
    }
}
