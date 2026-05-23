package domain;

/**
 * Clase base abstracta para elementos especiales del tablero.
 * Los elementos especiales son objetos estaticos que tienen un efecto
 * al ser tocados por un jugador o enemigo.
 * 
 * Una vez activados, desaparecen (used = true) y no vuelven a activarse.
 * Pueden resetearse al reiniciar el nivel.
 */
import java.io.Serializable;

public abstract class SpecialElement implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final Position position;
    protected boolean used;

    /**
     * @param position posicion del elemento en el tablero
     */
    public SpecialElement(Position position) {
        this.position = position;
        this.used = false;
    }

    /**
     * Efecto al ser tocado por un jugador.
     * @param player el jugador que lo toco
     */
    public abstract void onPlayerCollision(Player player);

    /**
     * Efecto al ser tocado por un enemigo.
     * @param enemy el enemigo que lo toco
     */
    public abstract void onEnemyCollision(Enemy enemy);

    /**
     * Reinicia el elemento para un nuevo nivel.
     */
    public void reset() {
        this.used = false;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isUsed() {
        return used;
    }
}
