package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.dto.match.MatchConfigRequestDTO;
import com.gabmene.videoguesser.dto.room.RoomResponseDTO;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EngineService {

    private final RestClient restClient;

    public void startMatchInEngine(Room roomStarting, MatchConfigRequestDTO request){
        try {
            String nodeEngineUrl = "http://localhost:3000/api/engine/lobby/"+roomStarting.getCode()+"/match";

            Map<String, Object> enginePayload = Map.of(
                    "maxRounds", request.getNumberOfRounds(),
                    "players", RoomResponseDTO.from(roomStarting).getPlayers(),
                    "prepDurationSeconds", AppConstants.ROUND_PREPARING_DURATION_SECONDS,
                    "guessingDurationSeconds", AppConstants.ROUND_GUESSING_DURATION_SECONDS,
                    "finishedDurationSeconds", AppConstants.ROUND_FINISHED_DURATION_SECONDS
            );

            restClient.post()
                    .uri(nodeEngineUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(enginePayload)
                    .retrieve()
                    .toBodilessEntity(); // Replaces postForEntity when you don't need the response body

            System.out.println("✅ Comando enviado: Node.js assumiu a sala " + roomStarting.getCode());

        } catch (Exception e) {
            System.err.println("❌ Falha crítica ao contactar o Árbitro Node.js: " + e.getMessage());
            throw new BusinessException("Servidor de partida indisponível no momento.");
        }
    }

    public void stopMatchInEngine(String roomCode){
        try{
            String nodeEngineUrl = "http://localhost:3000/api/engine/lobby/" + roomCode + "/match";
            restClient.delete().uri(nodeEngineUrl).retrieve().toBodilessEntity();
        } catch (Exception e) {
            System.err.println("❌ Error stopping match from node: " + e.getMessage());
        }
    }

    public void syncLobbyInEngine(RoomResponseDTO roomData){
        try {
            String nodeEngineUrl = "http://localhost:3000/api/engine/lobby/" + roomData.getCode();

            restClient.put()
                    .uri(nodeEngineUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(roomData)
                    .retrieve()
                    .toBodilessEntity(); // Replaces restTemplate.put()

            System.out.println(" Synching lobby from room " + roomData.getCode() + " with node: ");
        } catch (Exception e) {
            System.err.println("Failed to sync lobby with node: " + e.getMessage());
        }
    }

    public void deleteLobbyInEngine(String roomCode){
        try {
            String nodeEngineUrl = "http://localhost:3000/api/engine/lobby/" + roomCode;

            restClient.delete()
                    .uri(nodeEngineUrl)
                    .retrieve()
                    .toBodilessEntity(); // Replaces restTemplate.delete()

            System.out.println("🗑️ Delete lobby " + roomCode + " from node engine.");
        } catch (Exception e) {
            System.err.println("❌ Error deleting lobby from node: " + e.getMessage());
        }
    }
}
