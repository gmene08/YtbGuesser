package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Repository
public interface RoundRepository extends JpaRepository<Round,Integer> {

    @Query("SELECT r.video FROM Round r " +
            "JOIN r.match m " +
            "JOIN m.room room " +
            "WHERE room.code = :roomCode " +
            "AND m.status = 'PLAYING' " +
            "AND m.currentRound = r.roundNumber ")
    Optional<Video> findVideoByRoomCode(String roomCode);

    @Query("SELECT r FROM Round r " +
            "JOIN r.match m " +
            "WHERE m.id = :matchId " +
            "AND m.status = 'PLAYING' " +
            "AND m.currentRound = r.roundNumber")
    Optional<Round> findCurrentRoundByMatchId(Integer matchId);
}
