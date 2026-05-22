package com.gabmene.videoguesser.constants;

public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final Integer ROOM_MAX_PLAYERS_DEFAULT = 4;
    public static final Integer ROOM_MAX_PLAYERS_MIN = 2;
    public static final Integer ROOM_MAX_PLAYERS_MAX = 8;
    public static final Integer ROOM_CODE_LENGTH = 5;
    public static final Integer ROUND_GUESSING_DURATION_SECONDS = 10;
}
