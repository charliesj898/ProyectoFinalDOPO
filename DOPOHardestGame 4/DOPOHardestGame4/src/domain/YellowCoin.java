package domain;

/**
 * Moneda amarilla estandar.
 * Debe recolectarse junto con todas las demas monedas para completar el nivel.
 * No tiene efecto especial sobre el jugador.
 */
import java.io.Serializable;

public class YellowCoin extends Coin implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Crea una moneda amarilla en la posicion dada.
     * @param position posicion en el tablero
     */
    public YellowCoin(Position position) {
        super(position);
    }

    @Override
    public void onCollect(Player player) {
        this.collected = true;
        player.collectCoin();
    }

    @Override
    public CoinType getType() {
        return CoinType.YELLOW;
    }
}
