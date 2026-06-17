package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.dto.engine.EngineRoundEndResponseDTO;
import com.gabmene.videoguesser.dto.engine.EngineRoundReportDTO;
import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.service.GameService;
import com.gabmene.videoguesser.service.MatchService;
import com.gabmene.videoguesser.service.RoomService;
import com.gabmene.videoguesser.service.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/engine")
@RequiredArgsConstructor
public class EngineController {

    private final RoundService roundService;
    private final RoomService roomService;
    private final GameService gameService;
    private final MatchService matchService;

    @PatchMapping("/{roomCode}/status")
    public ResponseEntity<Void> updateRoundStatus(@PathVariable String roomCode, @RequestBody Map<String, String> request) {
        String statusStr = request.get("status");

        if (statusStr != null) {
            RoundStatus newStatus = RoundStatus.valueOf(statusStr.toUpperCase());
            roundService.updateStatusFromEngine(roomCode, newStatus);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomCode}/end-round")
    public ResponseEntity<MatchResponseDTO> processRoundResultsFromEngine(@RequestBody EngineRoundReportDTO report,@PathVariable String roomCode) {

        MatchResponseDTO response = gameService.processEngineReport(report, roomCode);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{roomCode}/change-round")
    public ResponseEntity<MatchResponseDTO> changeRoundsFromEngine(@PathVariable String roomCode){

        MatchResponseDTO response = matchService.changeToNextRound(roomCode);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<Void> disconnect(@RequestParam Integer userId){
        roomService.handleRoomDisconnect(userId);
        return ResponseEntity.ok().build();
    }

}

