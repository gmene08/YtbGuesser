package com.gabmene.videoguesser.dto.engine;
 // Certifique-se de usar os seus imports corretos
import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.dto.round.RoundResultResponseDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EngineRoundEndResponseDTO {
    private MatchResponseDTO matchData;
    private RoundResultResponseDTO roundResult;
}