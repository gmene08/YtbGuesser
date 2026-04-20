package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    List<User> findAllByRoom(Room room);
    Optional<User>  findByNickname(String nickname);
}
