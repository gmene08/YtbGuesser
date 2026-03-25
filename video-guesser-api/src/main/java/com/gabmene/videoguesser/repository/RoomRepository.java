package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room,Integer> {

    boolean existsByCode(String code);
}
