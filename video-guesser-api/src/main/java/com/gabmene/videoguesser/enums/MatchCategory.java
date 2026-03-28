package com.gabmene.videoguesser.enums;

import lombok.Getter;

@Getter
public enum MatchCategory {
    ALL(0),
    GAMING(1),
    SPORTS(2),
    MUSIC(3),
    VLOG(4);

    private final Integer id;

    MatchCategory(Integer id) {
        this.id = id;
    }

    // Method to get Category from ID
    public static MatchCategory fromId(Integer id) {
        for (MatchCategory category : MatchCategory.values()) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid Category ID: " + id);
    }
}
