package domain;

/**
 * Representa el tipo de una celda en el tablero del juego.
 * Las celdas definen la estructura estatica del nivel.
 */
public enum Cell {

    /** Celda vacia por la que se puede caminar. */
    EMPTY,

    /** Pared solida que no puede ser atravesada por nadie. */
    WALL,

    /** Zona de inicio del Jugador 1 / zona final del Jugador 2 en modos de 2 jugadores. */
    START,

    /** Zona segura intermedia que sirve como checkpoint de reaparicion. */
    SAFE_INTERMEDIATE,

    /** Zona segura final del Jugador 1 / zona de inicio del Jugador 2 en modos de 2 jugadores. */
    SAFE_FINAL,

    /**
     * Zona segura del Jugador 2 (equivalente a SAFE_FINAL pero para el lado opuesto del mapa).
     * En modos de 1 jugador este tipo no se usa.
     */
    SAFE_FINAL_2;

    /**
     * Indica si un jugador o entidad puede caminar sobre esta celda.
     * @return true si la celda es transitable (todo excepto WALL)
     */
    public boolean isWalkable() {
        return this != WALL;
    }
}
