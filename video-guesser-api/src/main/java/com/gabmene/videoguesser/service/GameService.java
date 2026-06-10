package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.engine.EngineRoundEndResponseDTO;
import com.gabmene.videoguesser.dto.engine.EngineRoundReportDTO;
import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.dto.round.ActiveRoundResponseDTO;
import com.gabmene.videoguesser.dto.round.RoundResultResponseDTO;
import com.gabmene.videoguesser.dto.round.UserGuessRequestDTO;
import com.gabmene.videoguesser.entity.*;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ConflictException;
import com.gabmene.videoguesser.exception.ForbiddenException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.RoundRepository;
import com.gabmene.videoguesser.repository.UserMatchRepository;
import com.gabmene.videoguesser.repository.UserRepository;
import com.gabmene.videoguesser.repository.UserRoundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final SimpMessagingTemplate messagingTemplate;

    private final RoundRepository roundRepository;
    private final UserRepository userRepository;
    private final UserRoundRepository userRoundRepository;
    private final UserMatchRepository userMatchRepository;
    private final MatchService matchService;
    private final RoundService roundService;

    private final GameNotificationService gameNotificationService;


    @Transactional
    public void processRoundResults(Round round) {

        // get the current total points for each user into a map
        Map<Integer,Integer> userCurrentTotalPoints = round.getMatch().getUserMatches().stream().collect(Collectors.toMap(
                userMatch -> userMatch.getUser().getId(),
                UserMatch::getCurrentScore
        ));

        List<UserMatch> userMatchesToBeUpdated = new ArrayList<>();

        // update the user matches with the new total points
        for(UserRound userGuess : round.getUserGuesses()) {
            Integer currentTotalPoints = userCurrentTotalPoints.getOrDefault(userGuess.getUser().getId(), 0);
            Integer pointsScored = userGuess.getPointsEarned();
            Integer newTotalPoints = currentTotalPoints + pointsScored;

            UserMatch userMatchToBeUpdated = userMatchRepository.findByUserIdAndMatchId(userGuess.getUser().getId(), round.getMatch().getId()).orElse(
                    UserMatch.builder()
                            .user(userGuess.getUser())
                            .match(round.getMatch())
                            .finalScore(0)
                            .currentScore(0)
                            .build()
            );

            userMatchToBeUpdated.setCurrentScore(newTotalPoints);
            userMatchesToBeUpdated.add(userMatchToBeUpdated);
        }
        userMatchRepository.saveAll(userMatchesToBeUpdated);
        //gameNotificationService.sendRoundResults(round.getId(), RoundResultResponseDTO.from(round));
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

    @Transactional
    public EngineRoundEndResponseDTO processEngineReport(EngineRoundReportDTO report) {

        Round currentRound = roundRepository.findCurrentRoundByRoomCode(report.getRoomCode())
                .orElseThrow(() -> new ResourceNotFoundException("Active round not found for room " + report.getRoomCode()));

        if (currentRound.getStatus() != RoundStatus.GUESSING) {
            throw new BusinessException("Round is not in guessing state to process results");
        }

        Long viewCount = currentRound.getVideo().getViewCount();


        for (EngineRoundReportDTO.EngineGuessDTO engineGuess : report.getGuesses()) {
            User user = userRepository.findById(engineGuess.getUserId()).orElse(null);
            if (user == null) continue; // Ignora se o usuário não existir

            // Fail-safe: garante que o usuário não seja salvo duas vezes no mesmo round
            if (userRoundRepository.existsByUserIdAndRoundId(user.getId(), currentRound.getId())) {
                continue;
            }

            Integer points = GameService.calculatePointsEarned(viewCount, engineGuess.getGuessValue());

            UserRound userRound = UserRound.builder()
                    .user(user)
                    .round(currentRound)
                    .lastGuess(engineGuess.getGuessValue())
                    .pointsEarned(points)
                    .build();

            userRoundRepository.save(userRound);
        }

        // 3. O Node já encerrou o tempo, então o Java oficializa o fim do round
        currentRound.setStatus(RoundStatus.FINISHED);
        roundRepository.save(currentRound);

        // 4. Chama o seu método que já existe para somar os pontos no placar geral (UserMatch)
        this.processRoundResults(currentRound);

        // 5. Constrói e retorna o DTO atualizado da partida para o Node.js enviar pro Angular
        // (Altere 'buildMatchResponseDTO' para o nome exato do método que você usa no MatchService para gerar o MatchDataResponse)
        MatchResponseDTO matchData = matchService.buildMatchResponseDTO(currentRound.getMatch());
        RoundResultResponseDTO roundResult = roundService.buildRoundResultResponseDTO(currentRound);

        // Retorna os dois juntos empacotados!
        return EngineRoundEndResponseDTO.builder()
                .matchData(matchData)
                .roundResult(roundResult)
                .build();
    }
}
