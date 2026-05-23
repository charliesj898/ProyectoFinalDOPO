package domain;

/**
 * Clase abstracta base para todos los enemigos del juego.
 * Los enemigos se mueven automaticamente siguiendo patrones especificos
 * y eliminan al jugador al contacto.
 * 
 * El sistema de velocidad usa un contador de ticks: el enemigo solo se mueve
 * cuando el contador alcanza el valor de ticksPerMove (derivado de la velocidad).
 */
import java.io.Serializable;

public abstract class Enemy implements Serializable {
    private static final long serialVersionUID = 1L;

    protected Position position;
    protected final Position initialPosition;
    protected final float speed;
    protected final int ticksPerMove;
    protected int tickCounter;

    /** Ticks base por movimiento a velocidad 1.0x */
    private static final int BASE_TICKS_PER_MOVE = 3;

    /**
     * Crea un enemigo en la posicion dada con el multiplicador de velocidad.
     * @param position posicion inicial
     * @param speed multiplicador de velocidad (1.0 = normal, 2.0 = doble)
     */
    public Enemy(Position position, float speed) {
        this.position = position;
        this.initialPosition = position;
        this.speed = speed;
        this.ticksPerMove = Math.max(1, Math.round(BASE_TICKS_PER_MOVE / speed));
        this.tickCounter = 0;
    }

    /**
     * Llamado en cada tick del juego. Maneja el timing de movimiento
     * y delega el movimiento real a la subclase cuando corresponde.
     * @param board el tablero del juego
     */
    public void update(Board board) {
        tickCounter++;
        if (tickCounter >= ticksPerMove) {
            move(board);
            tickCounter = 0;
        }
    }

    /**
     * Realiza la logica de movimiento especifica de cada tipo de enemigo.
     * @param board el tablero del juego
     */
    protected abstract void move(Board board);

    /**
     * Reinicia el enemigo a su estado inicial.
     */
    public abstract void reset();

    public Position getPosition() {
        return position;
    }

    public Position getInitialPosition() {
        return initialPosition;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " en " + position;
    }
}
