package com.gabmene.videoguesser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name="video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="youtube_id")
    private String youtubeId;

    @Column(name="title")
    private String title;

    @Column(name="view_count")
    private Long viewCount;

    @Column(name="channel_name")
    private String channelName;

    @Column(name="thumbnail_url")
    private String thumbnailUrl;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
