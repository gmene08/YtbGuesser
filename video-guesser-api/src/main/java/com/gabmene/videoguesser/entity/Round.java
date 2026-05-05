package com.gabmene.videoguesser.entity;

import com.gabmene.videoguesser.enums.RoundStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name="round")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="round_number")
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private RoundStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name="video_id")
    private Video video;

    @Column(name="ends_at")
    private Instant endsAt;

    @OneToMany (mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRound> userGuesses;

    @Column(name="video_starts_at_second")
    private Integer videoStartsAtSecond;



}
