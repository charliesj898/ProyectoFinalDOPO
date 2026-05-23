package domain;

/**
 * Modos de juego disponibles.
 */
public enum GameMode {

    /** Un solo jugador controla el cuadrado. */
    SINGLE_PLAYER("Un Jugador"),

    /** Dos jugadores compiten en el mismo tablero. */
    PLAYER_VS_PLAYER("Jugador vs Jugador"),

    /** Un jugador compite contra una maquina controlada por IA. */
    PLAYER_VS_MACHINE("Jugador vs Maquina");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
