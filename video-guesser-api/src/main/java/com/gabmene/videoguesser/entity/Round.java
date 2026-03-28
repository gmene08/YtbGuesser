package com.gabmene.videoguesser.entity;

import com.gabmene.videoguesser.enums.RoundStatus;
import jakarta.persistence.*;
import lombok.*;

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

}
