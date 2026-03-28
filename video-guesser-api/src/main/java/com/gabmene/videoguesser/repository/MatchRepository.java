package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match,Integer> {
}
