package com.gabmene.videoguesser.dto.match;

import com.gabmene.videoguesser.dto.player.PlayerCurrentScoreDTO;
import com.gabmene.videoguesser.dto.round.ActiveRoundResponseDTO;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.UserMatch;
import com.gabmene.videoguesser.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDTO {
    private Integer matchId;
    private Integer maxRounds;
    private Integer roundNumber;
    private MatchStatus status;
    private ActiveRoundResponseDTO currentRound;
    private List<PlayerCurrentScoreDTO> playerLeaderboard;

    public static MatchResponseDTO from(Match match) {
        if (match == null) {
            return null;
        }

        Round currentRound = match.getRounds().stream().filter
                (r -> r.getRoundNumber().equals(match.getCurrentRound())).findFirst().orElse(null);

        if(currentRound == null) {
            return null;
        }

        return new MatchResponseDTO(
                match.getId(),
                match.getNumberOfRounds(),
                match.getCurrentRound(),
                match.getStatus(),
                ActiveRoundResponseDTO.from(currentRound),
                mapPlayers(match.getUserMatches())
        );
    }

    private static List<PlayerCurrentScoreDTO> mapPlayers(List<UserMatch> userMatches){
        if(userMatches == null || userMatches.isEmpty()) {
            return null;
        }
        List<PlayerCurrentScoreDTO>  playerScoreUnsorted= userMatches
                .stream()
                .map(PlayerCurrentScoreDTO::from)
                .toList();

        // sort the list in descending order based on the total score
        return playerScoreUnsorted.stream().sorted((a,b) -> b.getTotalScore().compareTo(a.getTotalScore())).toList();

    }

}
