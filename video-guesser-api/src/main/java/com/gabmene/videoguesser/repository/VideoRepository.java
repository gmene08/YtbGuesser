package com.gabmene.videoguesser.repository;

import com.gabmene.videoguesser.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video,Integer> {

    @Query("SELECT v.youtubeId from Video v WHERE v.youtubeId IN :ids")
    List<String> findExistingYoutubeIds(@Param("ids") Collection<String> ids);
}
