package com.gabmene.videoguesser.dto.match;

import com.gabmene.videoguesser.dto.round.CurrentRoundResponseDTO;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDTO {
    private Integer id;
    private Integer maxRounds;
    private Integer roundNumber;
    private MatchStatus status;
    private CurrentRoundResponseDTO currentRound;


    public static MatchResponseDTO from(Match match) {
        Round currentRound = match.getRounds().stream().filter
                (r -> r.getRoundNumber().equals(match.getCurrentRound())).findFirst().orElse(null);

        CurrentRoundResponseDTO currentRoundDto = currentRound != null ?
                CurrentRoundResponseDTO.from(currentRound) : null;

        return new MatchResponseDTO(
                match.getId(),
                match.getNumberOfRounds(),
                match.getCurrentRound(),
                match.getStatus(),
                currentRoundDto
        );
    }
}
