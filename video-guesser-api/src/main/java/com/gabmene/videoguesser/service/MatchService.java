package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.match.MatchConfigRequestDTO;
import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.enums.MatchStatus;
import com.gabmene.videoguesser.repository.CategoryRepository;
import com.gabmene.videoguesser.repository.MatchRepository;
import com.gabmene.videoguesser.repository.RoomRepository;
import com.gabmene.videoguesser.repository.RoundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final CategoryRepository categoryRepository;
    private final MatchRepository matchRepository;
    private final RoomRepository roomRepository;

    private final RoundService roundService;

    @Transactional
    public Match createMatch(Room roomStarting, MatchConfigRequestDTO request){

        Match newMatch = new Match();

        Optional<Match> activeMatch = matchRepository.findByRoomAndStatus(roomStarting, MatchStatus.PLAYING);
        if(activeMatch.isPresent()) {
            return activeMatch.get();
        }

        newMatch.setRoom(roomStarting);
        newMatch.setCurrentRound(1);

        // set total rounds, if not set, default to 5
        newMatch.setNumberOfRounds(request.getNumberOfRounds());

        // set categories, if not set, default to all categories
        List<MatchCategory> selectedCategories = request.getCategories();

        // get categories from the database - if ALL is selected, get all categories
        List<Category> categories;
        if (selectedCategories.contains(MatchCategory.ALL)) {
            categories = categoryRepository.findAll();
        } else {
            categories = selectedCategories.stream()
                    .map(enumCat -> categoryRepository.findById(enumCat.getId())
                            .orElseThrow(() -> new RuntimeException("Category not found")))
                    .toList();
        }

        newMatch.setCategories(categories);
        newMatch.setStatus(MatchStatus.PLAYING);

        Match savedMatch = matchRepository.save(newMatch);

        // create all the rounds
        for (int i = 1; i <= newMatch.getNumberOfRounds(); i++) {
            roundService.createRound(savedMatch, i);
        }

        return matchRepository.save(savedMatch);
    }

    public Match getMatchByRoomCode(String roomCode){
        Room room = roomRepository.findByCode(roomCode).orElseThrow(()-> new RuntimeException("Room not Found"));

        // return the match that is currently being played in the room
        return matchRepository.findByRoomAndStatus(room, MatchStatus.PLAYING).orElseThrow(()-> new RuntimeException("Match not Found"));
    }

    public Match getMatchById(Integer matchId){
        return matchRepository.findById(matchId).orElseThrow(()-> new RuntimeException("Match not Found"));
    }
}
