package com.gabmene.videoguesser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="user_round", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "round_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="round_id")
    private Round round;

    @Column(name="last_guess")
    private Long lastGuess;

    @Column(name="points_earned")
    private Integer pointsEarned;
}
