package domain;

/**
 * Clase base abstracta para todas las monedas del juego.
 * Cada subclase define su comportamiento al ser recolectada mediante onCollect().
 * Esto permite agregar nuevos tipos de moneda sin modificar CollisionDetector.
 */
import java.io.Serializable;

public abstract class Coin implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final Position position;
    protected boolean collected;

    /**
     * Crea una moneda en la posicion dada.
     * @param position posicion de la moneda en el tablero
     */
    public Coin(Position position) {
        this.position = position;
        this.collected = false;
    }

    /**
     * Efecto al ser recolectada. Cada subclase implementa su logica.
     * Este metodo debe marcar la moneda como recolectada e incrementar
     * el contador del jugador.
     * @param player el jugador que recolecto la moneda
     */
    public abstract void onCollect(Player player);

    /**
     * Retorna el tipo de esta moneda para diferenciacion visual y logica.
     */
    public abstract CoinType getType();

    /**
     * Reinicia la moneda a estado no recolectado.
     */
    public void reset() {
        this.collected = false;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isCollected() {
        return collected;
    }

    /**
     * Define si la moneda esta recolectada (usado para restaurar partida).
     */
    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    @Override
    public String toString() {
        return getType() + " en " + position + (collected ? " (recolectada)" : "");
    }
}
