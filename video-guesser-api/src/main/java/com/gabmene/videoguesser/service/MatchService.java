package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.StartRoomRequestDTO;
import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.enums.MatchStatus;
import com.gabmene.videoguesser.repository.CategoryRepository;
import com.gabmene.videoguesser.repository.MatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final CategoryRepository categoryRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public Match createMatch(Room roomStarting, StartRoomRequestDTO request){
        Match newMatch = new Match();
        newMatch.setRoom(roomStarting);
        newMatch.setCurrentRound(1);

        newMatch.setNumberOfRounds(request.getNumberOfRounds() != null ?
                request.getNumberOfRounds()
                : 5);

        List<MatchCategory> selectedCategories = request.getCategory() != null
                ? request.getCategory()
                : List.of(MatchCategory.ALL);

        List<Category> categories = selectedCategories.stream()
                .map(enumCat -> categoryRepository.findById(enumCat.getId())
                        .orElseThrow(()-> new RuntimeException("Category not found")))
                .toList();

        newMatch.setCategories(categories);
        newMatch.setStatus(MatchStatus.PLAYING);

        return matchRepository.save(newMatch);
    }
}
