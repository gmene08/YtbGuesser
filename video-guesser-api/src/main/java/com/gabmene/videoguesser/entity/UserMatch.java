package com.gabmene.videoguesser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="user_match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="match_id")
    private Match match;

    @Column(name="current_score")
    private Integer currentScore;

    @Column(name="final_score")
    private Integer finalScore;

}
