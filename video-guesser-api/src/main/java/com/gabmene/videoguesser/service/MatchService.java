package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.match.MatchConfigRequestDTO;
import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.entity.*;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.enums.MatchStatus;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.*;
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
    private final UserMatchRepository userMatchRepository;

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
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found")))
                    .toList();
        }

        newMatch.setCategories(categories);
        newMatch.setStatus(MatchStatus.PLAYING);

        Match savedMatch = matchRepository.save(newMatch);

        // create all the rounds beforehand
        for (int i = 1; i <= newMatch.getNumberOfRounds(); i++) {
            roundService.createRound(savedMatch, i);
        }

        // create a UserMatch object for each user in the room
        for (User user : roomStarting.getUsers()) {
            UserMatch userMatch = new UserMatch();
            userMatch.setUser(user);
            userMatch.setMatch(savedMatch);
            userMatch.setFinalScore(0);
            userMatch.setCurrentScore(0);
            userMatchRepository.save(userMatch);
        }

        return matchRepository.save(savedMatch);
    }

    public Match getMatchByRoomCode(String roomCode){
        Room room = roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not Found"));

        // return the match that is currently being played in the room
        return matchRepository.findByRoomAndStatus(room, MatchStatus.PLAYING).orElseThrow(()-> new ResourceNotFoundException("Match not Found"));
    }

    public Match getMatchById(Integer matchId){
        return matchRepository.findById(matchId).orElseThrow(()-> new ResourceNotFoundException("Match not Found"));
    }
}
