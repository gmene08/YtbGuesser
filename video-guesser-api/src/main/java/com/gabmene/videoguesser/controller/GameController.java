package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.round.UserGuessRequestDTO;
import com.gabmene.videoguesser.dto.round.UserGuessResponseDTO;
import com.gabmene.videoguesser.entity.UserRound;
import com.gabmene.videoguesser.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GameController {
    private final GameService gameService;


}
