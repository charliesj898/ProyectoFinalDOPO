package domain;

/**
 * Estados posibles del juego.
 */
public enum GameState {

    /** Pantalla de menu principal. */
    MENU,

    /** El juego esta en curso. */
    PLAYING,

    /** El juego esta pausado. */
    PAUSED,

    /** Se completo el nivel actual. */
    LEVEL_COMPLETE,

    /** Se acabo el tiempo — fin del juego. */
    GAME_OVER,

    /** Se completaron todos los niveles — victoria total. */
    VICTORY
}
