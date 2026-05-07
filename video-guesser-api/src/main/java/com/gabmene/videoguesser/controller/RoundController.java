package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.round.ActiveRoundResponseDTO;
import com.gabmene.videoguesser.dto.round.UpdateRoundRequestDTO;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.service.GameService;
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
    private final GameService gameService;
    private final RoundService roundService;

    @PatchMapping("/{roundId}")
    public ResponseEntity<ActiveRoundResponseDTO> updateRound(@PathVariable Integer roundId, @Valid @RequestBody UpdateRoundRequestDTO request){
        Round round = roundService.updateRound(roundId, request);
        return ResponseEntity.ok(ActiveRoundResponseDTO.from(round));
    }

}
