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
    private final RoundService roundService;

    @Transactional
    public Match createMatch(Room roomStarting, StartRoomRequestDTO request){
        Match newMatch = new Match();
        newMatch.setRoom(roomStarting);
        newMatch.setCurrentRound(1);

        // set total rounds, if not set, default to 5
        newMatch.setNumberOfRounds(request.getNumberOfRounds() != null ?
                request.getNumberOfRounds()
                : 5);

        // set categories, if not set, default to all categories
        List<MatchCategory> selectedCategories = request.getCategory() != null
                ? request.getCategory()
                : List.of(MatchCategory.ALL);

        // get categories from the database
        List<Category> categories;
        if (selectedCategories.contains(MatchCategory.ALL)) { // if ALL is selected, get all categories from the database
            categories = categoryRepository.findAll();
        }else { // otherwise, get only the selected categories
            // get categories from the database
            categories = selectedCategories.stream()
                    .map(enumCat -> categoryRepository.findById(enumCat.getId())
                            .orElseThrow(() -> new RuntimeException("Category not found")))
                    .toList();
        }

        newMatch.setCategories(categories);
        newMatch.setStatus(MatchStatus.PLAYING);

        Match savedMatch = matchRepository.save(newMatch);

        roundService.createRound(savedMatch, 1);

        return savedMatch;
    }
}
