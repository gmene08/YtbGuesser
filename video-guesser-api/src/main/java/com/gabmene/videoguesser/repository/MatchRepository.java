package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match,Integer> {
    Optional<Match> findByRoomAndStatusInOrderByIdDesc(Room room, List<MatchStatus> status);

    @Query("SELECT m FROM Match m WHERE m.room.code = :roomCode AND m.status = 'PLAYING'")
    Optional<Match> findByRoomCode(String roomCode);

    Optional<Match> findByRoom(Room room);
}
