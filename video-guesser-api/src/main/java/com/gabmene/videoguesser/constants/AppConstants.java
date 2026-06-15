package com.gabmene.videoguesser.constants;

public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final int ROOM_MAX_PLAYERS_DEFAULT = 4;
    public static final int ROOM_MAX_PLAYERS_MIN = 1;
    public static final int ROOM_MAX_PLAYERS_MAX = 10;
    public static final int ROOM_CODE_LENGTH = 5;
    public static final int ROUND_GUESSING_DURATION_SECONDS = 3;
    public static final int ROUND_PREPARING_DURATION_SECONDS = 3;
    public static final int ROUND_FINISHED_DURATION_SECONDS = 5;
    public static final int TIME_FOR_ROOM_DISCONNECT_SECONDS = 20;
    public static final int TIME_FOR_USER_DELETION_HOURS = 24;
    public static final int JWT_EXPIRATION_TIME_SECONDS = 86400;
    public static final int TIME_BEFORE_FIRST_GUEST_DELETION_MINUTES = 10;

    public static final int MAX_NICKNAME_LENGTH = 16;
    public static final int MIN_NICKNAME_LENGTH = 3;
}
