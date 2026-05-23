package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IA aleatoria: elige en cada tick una direccion cardinal valida al azar.
 * Si esta en la zona final y tiene todas las monedas, se detiene.
 */
import java.io.Serializable;

public class RandomAIController extends AIController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Direction getNextDirection(Player player, Level level) {
        if (player.hasReachedGoal()) {
            return null;
        }

        List<Direction> cardinals = new ArrayList<>(List.of(Direction.cardinals()));
        Collections.shuffle(cardinals);

        Board board = level.getBoard();
        Position pos = player.getPosition();

        for (Direction dir : cardinals) {
            if (board.isWalkable(pos.move(dir))) {
                return dir;
            }
        }
        return null;
    }
}
