package com.gabmene.videoguesser.entity;

import com.gabmene.videoguesser.enums.MatchCategory;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name="category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Category implements Persistable<Integer> {

    @Id
    @Column(name="id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name="name")
    private MatchCategory name;

    @Column(name="slug")
    private String slug;

    @Transient
    private boolean isNew = true;

    public Category(Integer id, MatchCategory name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
