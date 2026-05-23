package domain;

/**
 * Bomba: elemento especial estatico.
 * Destruye cualquier elemento que pase por ella:
 * - Si un jugador la toca: muere (handleEnemyCollision sin escudo).
 * - Si un enemigo la toca: se resetea a su posicion inicial.
 * Desaparece una vez activada.
 */
import java.io.Serializable;

public class Bomb extends SpecialElement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Crea una bomba en la posicion dada.
     * @param position posicion en el tablero
     */
    public Bomb(Position position) {
        super(position);
    }

    @Override
    public void onPlayerCollision(Player player) {
        if (!used) {
            used = true;
            GameLogger.info("Bomba activada por " + player.getName());
            // La bomba mata ignorando escudo: forzamos die publicamente via handleBombCollision
            player.handleBombCollision();
        }
    }

    @Override
    public void onEnemyCollision(Enemy enemy) {
        if (!used) {
            used = true;
            GameLogger.info("Bomba destruyo al enemigo en " + enemy.getPosition());
            enemy.reset();
        }
    }
}
