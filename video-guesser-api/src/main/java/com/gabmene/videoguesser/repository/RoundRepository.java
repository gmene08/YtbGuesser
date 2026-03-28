package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundRepository extends JpaRepository<Round,Integer> {
}
