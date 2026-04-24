package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Round;
import com.gabmene.videoguesser.entity.UserRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRoundRepository extends JpaRepository<UserRound, Integer> {

    @Query("SELECT ur FROM UserRound ur WHERE ur.user.id = :userId AND ur.round.id = :roundId")
    Optional<UserRound> findByUserIdAndRoundId(@Param("userId") Integer userId, @Param("roundId")Integer roundId);

    boolean existsByUserIdAndRoundId(Integer userId, Integer roundId);
}
