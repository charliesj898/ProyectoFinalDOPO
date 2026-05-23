package domain;

/**
 * Enemigo deslizador vertical (Tipo V).
 * Se desplaza exclusivamente en linea recta vertical y rebota
 * al chocar con paredes o los limites superior/inferior del tablero.
 * Velocidad: 1.0x (normal).
 */
import java.io.Serializable;

public class VerticalEnemy extends Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    private Direction currentDirection;
    private final Direction initialDirection;

    /**
     * Crea un deslizador vertical.
     * @param position  posicion inicial
     * @param direction direccion inicial: solo UP o DOWN
     */
    public VerticalEnemy(Position position, Direction direction) {
        super(position, 1.0f);
        // Forzar que la direccion sea vertical
        this.currentDirection = (direction == Direction.UP || direction == Direction.DOWN)
                ? direction : Direction.DOWN;
        this.initialDirection = this.currentDirection;
    }

    @Override
    protected void move(Board board) {
        Position next = position.move(currentDirection);

        if (board.isWalkable(next)) {
            position = next;
        } else {
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
}
