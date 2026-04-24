package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MatchController {
    private final MatchService matchService;

    @GetMapping("/")
    public ResponseEntity<MatchResponseDTO> getMatchBeingPlayedByRoomCode(@RequestParam String roomCode){
        Match match = matchService.getMatchByRoomCode(roomCode);
        return ResponseEntity.ok(MatchResponseDTO.from(match));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponseDTO> getMatchBeingPlayedByMatchId(@PathVariable Integer matchId){
        Match match = matchService.getMatchById(matchId);
        return ResponseEntity.ok(MatchResponseDTO.from(match));
    }


}
