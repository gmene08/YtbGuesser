package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.dto.match.MatchConfigRequestDTO;
import com.gabmene.videoguesser.dto.match.MatchResponseDTO;
import com.gabmene.videoguesser.dto.room.RoomResponseDTO;
import com.gabmene.videoguesser.entity.*;
import com.gabmene.videoguesser.enums.MatchCategory;
import com.gabmene.videoguesser.enums.MatchStatus;
import com.gabmene.videoguesser.enums.RoomStatus;
import com.gabmene.videoguesser.exception.BusinessException;
import com.gabmene.videoguesser.exception.ResourceNotFoundException;
import com.gabmene.videoguesser.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final CategoryRepository categoryRepository;
    private final MatchRepository matchRepository;
    private final RoomRepository roomRepository;
    private final UserMatchRepository userMatchRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final RoundService roundService;
    private final GameNotificationService gameNotificationService;


    @Transactional
    public Match createMatch(Room roomStarting, MatchConfigRequestDTO request){

        Match newMatch = new Match();

        // if there is already a not finished match in the room, return the existing match
        Optional<Match> activeMatch = matchRepository.findByRoomAndStatusInOrderByIdDesc(roomStarting,List.of(MatchStatus.PLAYING, MatchStatus.RESULTS) );
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

    @Transactional
    public MatchResponseDTO changeToNextRound(String roomCode){
        Match match = matchRepository.findByRoomCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Match not Found"));

        if(match.getStatus() != MatchStatus.PLAYING) {
            throw new BusinessException("Match is not in PLAYING status");
        }

        // if the current round is the last round, set the match status to FINISHED -- otherwise, increment the current round
        int currentRound = match.getCurrentRound();
        if(currentRound >= match.getNumberOfRounds()) {
            match.setStatus(MatchStatus.RESULTS);
        }
        else {
            match.setCurrentRound(currentRound + 1);
        }

        // save the match
        Match savedMatch = matchRepository.save(match);

        //gameNotificationService.sendMatchUpdate(this.buildMatchResponseDTO(savedMatch));

        return this.buildMatchResponseDTO(savedMatch);
    }

    public Match getMatchByRoomCode(String roomCode){
        Room room = roomRepository.findByCode(roomCode).orElseThrow(()-> new ResourceNotFoundException("Room not Found"));

        return matchRepository.findByRoomAndStatusInOrderByIdDesc(room, List.of(MatchStatus.PLAYING, MatchStatus.RESULTS)).orElseThrow(()-> new ResourceNotFoundException("Match not Found"));
    }

    public Match getMatchById(Integer matchId){

        return matchRepository.findById(matchId).orElseThrow(()-> new ResourceNotFoundException("Match not Found"));
    }



    @Transactional
    public void endMatch(Integer matchId, Integer userId) {
        Match match = matchRepository.findById(matchId).orElseThrow(()-> new ResourceNotFoundException("Match not Found"));

        if(!Objects.equals(match.getRoom().getOwner().getId(), userId)) {
            throw new BusinessException("User is not the owner of the room");
        }

        /*
        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);*/

        Room room = match.getRoom();

        // if the room is in PLAYING status, set it to WAITING status -- So players can join again and return to the lobby
        if(room.getStatus() == RoomStatus.PLAYING) {
            room.setStatus(RoomStatus.WAITING);
            Room roomSaved = roomRepository.save(room);

            gameNotificationService.sendRoomUpdate(RoomResponseDTO.from(roomSaved));
        }

        matchRepository.delete(match); // delete the match to save database space for now


    }

    public MatchResponseDTO buildMatchResponseDTO(Match match) {
        List<UserMatch> sortedLeaderboard = userMatchRepository.findAllByMatchOrderByCurrentScoreDesc(match);

        return MatchResponseDTO.from(match, sortedLeaderboard);
    }
}
