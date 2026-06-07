package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Room;
import com.gabmene.videoguesser.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room,Integer> {

    boolean existsByCode(String code);

    @Query("SELECT r.code FROM User u JOIN u.room r WHERE u.id = :userId")
    Optional<String> findRoomCodeByUserId(Integer userId);


    Optional<Room> findByCode(String code);
    Optional<Room> findByOwner(User owner);
}
