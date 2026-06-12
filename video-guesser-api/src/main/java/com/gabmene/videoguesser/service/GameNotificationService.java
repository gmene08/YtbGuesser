/*package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.dto.room.RoomResponseDTO;
import com.gabmene.videoguesser.dto.round.ActiveRoundResponseDTO;
import com.gabmene.videoguesser.dto.round.RoundResultResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class GameNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendRoomUpdate(RoomResponseDTO room) {
        if (TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/room/" + room.getCode() + "/lobby", room);
                    System.out.println("Sending room update to " + room.getCode());
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/room/" + room.getCode() + "/lobby", room);
            System.out.println("Sending room update to " + room.getCode());
        }
    }

    public void sendMatchUpdate(MatchResponseDTO match) {
        // if the transaction is active, register a synchronization to send the message after the transaction is committed
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/game/match/" + match.getMatchId() + "/match-data", match);
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/game/match/" + match.getMatchId() + "/match-data", match);
        }

    }

    public void sendRoundUpdate(ActiveRoundResponseDTO round) {

        // check if the transaction is active, if it is, register a synchronization to send the message after the transaction is committed
        if(TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/game/round/" + round.getRoundId() + "/round-status", round);
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/game/round/" + round.getRoundId() + "/round-status", round);
        }

    }


    public void sendRoundResults(Integer roundId, RoundResultResponseDTO roundResults) {
        messagingTemplate.convertAndSend("/topic/game/round/" + roundId + "/round-results", roundResults);
    }
}*/