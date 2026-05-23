package domain;

/**
 * Representa las direcciones de movimiento disponibles.
 * Incluye las cuatro direcciones cardinales y las cuatro diagonales.
 * Cada direccion tiene un delta de fila y columna asociado.
 */
public enum Direction {

    // Cardinales
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1),

    // Diagonales
    UP_LEFT(-1, -1),
    UP_RIGHT(-1, 1),
    DOWN_LEFT(1, -1),
    DOWN_RIGHT(1, 1);

    private final int deltaRow;
    private final int deltaCol;

    Direction(int deltaRow, int deltaCol) {
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    public int getDeltaRow() {
        return deltaRow;
    }

    public int getDeltaCol() {
        return deltaCol;
    }

    /**
     * Retorna la direccion opuesta.
     */
    public Direction opposite() {
        switch (this) {
            case UP:         return DOWN;
            case DOWN:       return UP;
            case LEFT:       return RIGHT;
            case RIGHT:      return LEFT;
            case UP_LEFT:    return DOWN_RIGHT;
            case UP_RIGHT:   return DOWN_LEFT;
            case DOWN_LEFT:  return UP_RIGHT;
            case DOWN_RIGHT: return UP_LEFT;
            default:         return this;
        }
    }

    /**
     * Retorna solo las cuatro direcciones cardinales.
     * Util para logica de enemigos que no se mueven en diagonal.
     */
    public static Direction[] cardinals() {
        return new Direction[]{UP, DOWN, LEFT, RIGHT};
    }
}
