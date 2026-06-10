package com.gabmene.videoguesser.dto.engine;
import lombok.Data;
import java.util.List;

@Data
public class EngineRoundReportDTO {
    private String roomCode;
    private List<EngineGuessDTO> guesses;

    @Data
    public static class EngineGuessDTO {
        private Integer userId;
        private Long guessValue;
    }
}
