package com.gabmene.videoguesser.dto;

import com.gabmene.videoguesser.enums.MatchCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchConfigRequestDTO {
    @NotNull (message = "User id cannot be null")
    private Integer userId; // ownerId that is trying to start the room

    // Match configurations
    @NotNull
    @Min(value = 1, message = "Number of rounds must be greater than 0")
    @Max(value = 10, message = "Number of rounds must be at most 10")
    private Integer numberOfRounds;

    @NotEmpty (message = "At least one category must be selected")
    private List<MatchCategory> categories;
}
