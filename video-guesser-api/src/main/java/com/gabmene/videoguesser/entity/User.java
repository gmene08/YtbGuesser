package com.gabmene.videoguesser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="user")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "room")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="nickname")
    private String nickname;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    @Column(name="is_guest")
    private Boolean isGuest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id")
    private Room room;


}
