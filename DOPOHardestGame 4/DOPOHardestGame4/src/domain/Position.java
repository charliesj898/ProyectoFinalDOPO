package domain;

/**
 * Representa una posicion inmutable en el tablero del juego.
 * Usa coordenadas de fila (vertical) y columna (horizontal).
 */
import java.io.Serializable;

public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Retorna una nueva Position movida un paso en la direccion dada.
     * @param direction la direccion de movimiento
     * @return nueva posicion resultante
     */
    public Position move(Direction direction) {
        return new Position(row + direction.getDeltaRow(), col + direction.getDeltaCol());
    }

    /**
     * Calcula la distancia Manhattan hacia otra posicion.
     * @param other la posicion destino
     * @return distancia Manhattan (|dRow| + |dCol|)
     */
    public int distanceTo(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
