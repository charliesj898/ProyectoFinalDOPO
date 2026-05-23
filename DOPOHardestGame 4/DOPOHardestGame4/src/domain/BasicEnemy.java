package domain;

/**
 * Enemigo que se desplaza en linea recta (horizontal o vertical)
 * y rebota al chocar con paredes o los limites del tablero.
 * Velocidad: 1.0x (normal).
 */
import java.io.Serializable;

public class BasicEnemy extends Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    private Direction currentDirection;
    private final Direction initialDirection;

    /**
     * Crea un enemigo basico con movimiento lineal.
     * @param position posicion inicial
     * @param direction direccion inicial de movimiento (UP, DOWN, LEFT o RIGHT)
     */
    public BasicEnemy(Position position, Direction direction) {
        super(position, 1.0f);
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
            // Si tampoco puede moverse, se queda en su lugar
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
