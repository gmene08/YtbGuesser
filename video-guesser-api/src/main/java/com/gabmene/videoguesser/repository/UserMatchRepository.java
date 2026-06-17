package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Match;
import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.entity.UserMatch;
import com.gabmene.videoguesser.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMatchRepository extends JpaRepository<UserMatch,Integer> {

    List<UserMatch> findAllByUser(User user);

    List<UserMatch> user(User user);

    @Query("SELECT um FROM UserMatch um WHERE um.user.id = :userId AND um.match.id = :matchId")
    Optional<UserMatch> findByUserIdAndMatchId(@Param("userId") Integer userId, @Param("matchId") Integer matchId);

    @Query("SELECT um FROM UserMatch um WHERE um.user.id = :userId AND um.match.room.id = :roomId AND um.match.status != :status")
    Optional<UserMatch> findByUserIdAndRoomIdAndStatusNot(@Param("userId") Integer userId, @Param("roomId") Integer roomId, MatchStatus status);

    List<UserMatch> findAllByMatchOrderByCurrentScoreDesc(Match match);
}
