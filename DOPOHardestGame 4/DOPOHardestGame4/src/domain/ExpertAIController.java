package domain;

import java.util.List;

/**
 * IA experta: busca la moneda no recolectada mas cercana (distancia Manhattan)
 * y se mueve hacia ella. Cuando todas las monedas estan recolectadas,
 * se dirige hacia la zona segura final que le corresponde.
 * Se detiene al llegar a su meta.
 */
import java.io.Serializable;

public class ExpertAIController extends AIController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Direction getNextDirection(Player player, Level level) {
        if (player.hasReachedGoal()) {
            return null;
        }

        Position playerPos = player.getPosition();
        Position target = findTarget(playerPos, level, player.getGoalCell());

        if (target == null) {
            return null;
        }

        return calculateDirection(playerPos, target, level.getBoard());
    }

    /**
     * Busca el objetivo: la moneda mas cercana o la zona segura final del jugador.
     */
    private Position findTarget(Position playerPos, Level level, Cell goalCell) {
        Position closestCoin = null;
        int minDistance = Integer.MAX_VALUE;

        for (Coin coin : level.getCoins()) {
            if (!coin.isCollected()) {
                int distance = playerPos.distanceTo(coin.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCoin = coin.getPosition();
                }
            }
        }

        if (closestCoin != null) {
            return closestCoin;
        }

        return findGoalCell(level.getBoard(), goalCell);
    }

    /**
     * Busca la primera celda del tipo indicado en el tablero.
     */
    private Position findGoalCell(Board board, Cell goalCell) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c) == goalCell) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    /**
     * Calcula la mejor direccion hacia el objetivo.
     * Prioriza el eje con mayor distancia; si esta bloqueado, prueba el otro.
     */
    private Direction calculateDirection(Position from, Position to, Board board) {
        int dRow = to.getRow() - from.getRow();
        int dCol = to.getCol() - from.getCol();

        if (Math.abs(dRow) >= Math.abs(dCol)) {
            Direction vertical = dRow > 0 ? Direction.DOWN : dRow < 0 ? Direction.UP : null;
            Direction horizontal = dCol > 0 ? Direction.RIGHT : dCol < 0 ? Direction.LEFT : null;
            if (vertical != null && board.isWalkable(from.move(vertical)))
                return vertical;
            if (horizontal != null && board.isWalkable(from.move(horizontal)))
                return horizontal;
        } else {
            Direction horizontal = dCol > 0 ? Direction.RIGHT : dCol < 0 ? Direction.LEFT : null;
            Direction vertical = dRow > 0 ? Direction.DOWN : dRow < 0 ? Direction.UP : null;
            if (horizontal != null && board.isWalkable(from.move(horizontal)))
                return horizontal;
            if (vertical != null && board.isWalkable(from.move(vertical)))
                return vertical;
        }

        for (Direction dir : Direction.cardinals()) {
            if (board.isWalkable(from.move(dir))) {
                return dir;
            }
        }
        return null;
    }
}
