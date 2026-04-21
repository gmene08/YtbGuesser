package com.gabmene.videoguesser.entity;

import com.gabmene.videoguesser.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="number_of_rounds")
    private int numberOfRounds;

    @Column(name="current_round")
    private int currentRound;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private MatchStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id")
    private Room room;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="match_category", joinColumns = @JoinColumn(name="match_id"), inverseJoinColumns = @JoinColumn(name="category_id"))
    private List<Category> categories;

    @OneToMany(mappedBy = "match",fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Round> rounds;

}
