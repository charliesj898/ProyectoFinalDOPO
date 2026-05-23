package domain;

/**
 * Moneda que cambia temporalmente el skin (tipo) del jugador al ser recolectada.
 * El efecto dura hasta que el jugador muere o recolecta otra moneda.
 * Tambien cuenta como moneda recolectada para la condicion de victoria.
 */
import java.io.Serializable;

public class SkinCoin extends Coin implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CoinType type;
    private final PlayerType skinType;

    /**
     * Crea una moneda skin del tipo dado.
     * @param position posicion en el tablero
     * @param type     tipo de moneda (RED_SKIN, BLUE_SKIN o GREEN_SKIN)
     */
    public SkinCoin(Position position, CoinType type) {
        super(position);
        this.type = type;
        this.skinType = resolveSkinType(type);
    }

    @Override
    public void onCollect(Player player) {
        this.collected = true;
        player.collectCoin();
        player.applySkinEffect(skinType);
    }

    @Override
    public CoinType getType() {
        return type;
    }

    /**
     * Mapea el CoinType al PlayerType correspondiente.
     */
    private PlayerType resolveSkinType(CoinType coinType) {
        switch (coinType) {
            case RED_SKIN:   return PlayerType.RED;
            case BLUE_SKIN:  return PlayerType.BLUE;
            case GREEN_SKIN: return PlayerType.GREEN;
            default:         return PlayerType.RED;
        }
    }
}
