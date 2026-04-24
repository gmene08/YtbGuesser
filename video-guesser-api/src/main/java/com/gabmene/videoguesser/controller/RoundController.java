package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.round.UserGuessRequestDTO;
import com.gabmene.videoguesser.entity.UserRound;
import com.gabmene.videoguesser.service.RoundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/round")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RoundController {
    private final RoundService roundService;

}
