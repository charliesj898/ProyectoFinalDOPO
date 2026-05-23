package domain;

/**
 * Enemigo que se mueve como un BasicEnemy pero al doble de velocidad.
 * Se desplaza en linea recta y rebota al chocar con paredes.
 * Velocidad: 2.0x (doble).
 */
import java.io.Serializable;

public class FastEnemy extends Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    private Direction currentDirection;
    private final Direction initialDirection;

    /**
     * Crea un enemigo rapido con movimiento lineal a velocidad 2x.
     * @param position posicion inicial
     * @param direction direccion inicial de movimiento
     */
    public FastEnemy(Position position, Direction direction) {
        super(position, 2.0f);
        this.currentDirection = direction;
        this.initialDirection = direction;
    }

    @Override
    protected void move(Board board) {
        Position next = position.move(currentDirection);

        if (board.isWalkable(next)) {
            position = next;
        } else {
            // Rebote: invertir direccion e intentar moverse
            currentDirection = currentDirection.opposite();
            next = position.move(currentDirection);
            if (board.isWalkable(next)) {
                position = next;
            }
        }
    }

    @Override
    public void reset() {
        this.position = initialPosition;
        this.currentDirection = initialDirection;
        this.tickCounter = 0;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }
}
