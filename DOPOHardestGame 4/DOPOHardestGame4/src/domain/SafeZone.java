package domain;

/**
 * Representa una zona segura en el tablero.
 * Puede ser intermedia (checkpoint de reaparicion) o final (completar nivel).
 */
public class SafeZone {

    private final Position position;
    private final boolean intermediate;

    /**
     * Crea una zona segura.
     * @param position posicion de la zona en el tablero
     * @param intermediate true si es zona intermedia (checkpoint), false si es zona final
     */
    public SafeZone(Position position, boolean intermediate) {
        this.position = position;
        this.intermediate = intermediate;
    }

    /**
     * Indica si es una zona segura intermedia (checkpoint de reaparicion).
     */
    public boolean isIntermediate() {
        return intermediate;
    }

    /**
     * Indica si es la zona segura final (para completar el nivel).
     */
    public boolean isFinal() {
        return !intermediate;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return (intermediate ? "Intermedia" : "Final") + " SafeZone en " + position;
    }
}
