package domain;

/**
 * Fuente de vida: elemento especial estatico.
 * Al ser tocada por un jugador, le otorga la capacidad de absorber
 * un golpe de enemigo sin morir (igual que el escudo del jugador GREEN).
 * Desaparece una vez usada.
 * Los enemigos la ignoran (no tienen efecto sobre ellos).
 */
import java.io.Serializable;

public class LifeSource extends SpecialElement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Crea una fuente de vida en la posicion dada.
     * @param position posicion en el tablero
     */
    public LifeSource(Position position) {
        super(position);
    }

    @Override
    public void onPlayerCollision(Player player) {
        if (!used) {
            used = true;
            player.activateShield();
            GameLogger.info("Fuente de vida recogida por " + player.getName());
        }
    }

    @Override
    public void onEnemyCollision(Enemy enemy) {
        // Las fuentes de vida no afectan a los enemigos
    }
}
