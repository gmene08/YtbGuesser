package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.dto.room.RoomResponseDTO;
import com.gabmene.videoguesser.dto.round.ActiveRoundResponseDTO;
import com.gabmene.videoguesser.dto.round.RoundResultResponseDTO;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.Round;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class GameNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendRoomUpdate(Room room) {
        RoomResponseDTO roomData = RoomResponseDTO.from(room);
        if (TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/room/" + room.getCode() + "/lobby", roomData);
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/room/" + room.getCode() + "/lobby", roomData);
        }
    }

    public void sendMatchUpdate(Match match) {
        MatchResponseDTO matchResponseDTO = MatchResponseDTO.from(match);

        // if the transaction is active, register a synchronization to send the message after the transaction is committed
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/game/match/" + match.getId() + "/match-data", matchResponseDTO);
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/game/match/" + match.getId() + "/match-data", matchResponseDTO);
        }

    }

    public void sendRoundUpdate(Round round) {

        ActiveRoundResponseDTO roundData = ActiveRoundResponseDTO.from(round);

        // check if the transaction is active, if it is, register a synchronization to send the message after the transaction is committed
        if(TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/game/round/" + round.getId() + "/round-status", roundData);
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/game/round/" + round.getId() + "/round-status", roundData);
        }

    }


    public void sendRoundResults(Round round) {
        RoundResultResponseDTO roundResults = RoundResultResponseDTO.from(round);
        messagingTemplate.convertAndSend("/topic/game/round/" + round.getId() + "/round-results", roundResults);
    }
}