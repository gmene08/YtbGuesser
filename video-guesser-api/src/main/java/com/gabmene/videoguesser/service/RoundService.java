package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.enums.RoundStatus;
import com.gabmene.videoguesser.repository.RoundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepository;
    private final MatchService matchService;

    @Transactional
    public Round createRound(Match match, Integer roundNumber){
        Round round = Round.builder()
                .roundNumber(roundNumber)
                .match(match)
                .status(RoundStatus.PREPARING)
                .build();

        // TODO: random video generation logic


        // room.setVideo(video);

        return roundRepository.save(round);
    }
}
