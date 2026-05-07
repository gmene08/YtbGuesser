package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.User;
import com.gabmene.videoguesser.entity.UserMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMatchRepository extends JpaRepository<UserMatch,Integer> {

    List<UserMatch> findAllByUser(User user);

    List<UserMatch> user(User user);

    Optional<UserMatch> findByUserIdAndMatchId(Integer id, Integer id1);
}
