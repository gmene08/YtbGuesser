package com.gabmene.videoguesser.entity;

import com.gabmene.videoguesser.enums.MatchCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Category {

    @Id
    @Column(name="id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name="name")
    private MatchCategory name;

    @Column(name="slug")
    private String slug;


}
