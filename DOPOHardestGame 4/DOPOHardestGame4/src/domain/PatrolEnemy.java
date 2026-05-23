package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Enemigo que patrulla una ruta predefinida siguiendo waypoints en ciclo.
 * Se mueve de waypoint en waypoint, y al llegar al ultimo vuelve al primero.
 * Velocidad: 1.0x (normal).
 */
import java.io.Serializable;

public class PatrolEnemy extends Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Position> waypoints;
    private int currentWaypointIndex;

    /**
     * Crea un enemigo patrullero que sigue los waypoints dados.
     * @param startPosition posicion inicial
     * @param waypoints lista de posiciones a visitar en orden (se recorren ciclicamente)
     */
    public PatrolEnemy(Position startPosition, List<Position> waypoints) {
        super(startPosition, 1.0f);
        this.waypoints = new ArrayList<>(waypoints);
        this.currentWaypointIndex = 0;
    }

    @Override
    protected void move(Board board) {
        if (waypoints.isEmpty()) {
            return;
        }

        Position target = waypoints.get(currentWaypointIndex);

        // Si ya llego al waypoint actual, avanzar al siguiente
        if (position.equals(target)) {
            currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
            target = waypoints.get(currentWaypointIndex);
        }

        // Moverse una celda hacia el objetivo
        int dRow = Integer.compare(target.getRow(), position.getRow());
        int dCol = Integer.compare(target.getCol(), position.getCol());

        // Priorizar movimiento vertical, luego horizontal
        if (dRow != 0) {
            Position next = new Position(position.getRow() + dRow, position.getCol());
            if (board.isWalkable(next)) {
                position = next;
            }
        } else if (dCol != 0) {
            Position next = new Position(position.getRow(), position.getCol() + dCol);
            if (board.isWalkable(next)) {
                position = next;
            }
        }
    }

    @Override
    public void reset() {
        this.position = initialPosition;
        this.currentWaypointIndex = 0;
        this.tickCounter = 0;
    }

    public List<Position> getWaypoints() {
        return waypoints;
    }
}
