package domain;

import java.util.List;

/**
 * Motor de movimiento que se encarga de actualizar las posiciones
 * de todos los enemigos en cada tick del juego.
 * 
 * Separa la logica de actualizacion de enemigos del controlador principal (Game),
 * manteniendo las responsabilidades claras.
 */
import java.io.Serializable;

public class MovementEngine implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Actualiza todos los enemigos del nivel en un tick.
     * Cada enemigo maneja internamente su propio timing de movimiento.
     * @param enemies lista de enemigos a actualizar
     * @param board tablero del juego (para verificar colisiones con paredes)
     */
    public void update(List<Enemy> enemies, Board board) {
        for (Enemy enemy : enemies) {
            enemy.update(board);
        }
    }
}
