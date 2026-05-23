package domain;

import java.util.List;

/**
 * Detecta y procesa colisiones entre todas las entidades del juego.
 *
 * Tipos de colision manejados:
 * - Jugador vs. Enemigo
 * - Jugador vs. Moneda   (delega logica a coin.onCollect)
 * - Jugador vs. Zona segura
 * - Jugador vs. Jugador  (modos PvsP / PvsM)
 * - Jugador vs. Elemento especial
 * - Enemigo vs. Elemento especial
 */
import java.io.Serializable;

public class CollisionDetector implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Revisa si algun enemigo colisiona con el jugador.
     */
    public boolean checkPlayerEnemyCollision(Player player, List<Enemy> enemies) {
        Position playerPos = player.getPosition();
        for (Enemy enemy : enemies) {
            if (playerPos.equals(enemy.getPosition())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Revisa y procesa colisiones del jugador con monedas.
     * Delega el efecto a cada Coin mediante onCollect(player).
     * @return cantidad de monedas recolectadas en esta revision
     */
    public int checkPlayerCoinCollision(Player player, List<Coin> coins) {
        Position playerPos = player.getPosition();
        int collected = 0;
        for (Coin coin : coins) {
            if (!coin.isCollected() && playerPos.equals(coin.getPosition())) {
                // Antes de aplicar nuevo skin, resetear el anterior
                if (coin instanceof SkinCoin) {
                    player.resetSkin();
                }
                coin.onCollect(player);
                collected++;
            }
        }
        return collected;
    }

    /**
     * Revisa si el jugador esta en una zona segura del tablero.
     * - Zona intermedia: actualiza el punto de reaparicion.
     * - goalCell del jugador: indica que llego al objetivo.
     * @return tipo de celda segura en la que esta el jugador, o null
     */
    public Cell checkPlayerSafeZone(Player player, Board board) {
        Cell currentCell = board.getCell(player.getPosition());

        if (currentCell == Cell.SAFE_INTERMEDIATE) {
            player.updateRespawnPoint(player.getPosition());
            return Cell.SAFE_INTERMEDIATE;
        }

        // Cada jugador tiene su propia celda objetivo
        if (currentCell == player.getGoalCell()) {
            return player.getGoalCell();
        }

        return null;
    }

    /**
     * Revisa si dos jugadores colisionan entre si.
     * @return true si P1 y P2 estan en la misma celda
     */
    public boolean checkPlayerPlayerCollision(Player p1, Player p2) {
        return p1.getPosition().equals(p2.getPosition());
    }

    /**
     * Procesa colisiones del jugador con elementos especiales.
     * Los elementos usados son ignorados.
     */
    public void checkPlayerSpecialElementCollisions(Player player,
            List<SpecialElement> specialElements) {
        Position playerPos = player.getPosition();
        for (SpecialElement se : specialElements) {
            if (!se.isUsed() && playerPos.equals(se.getPosition())) {
                se.onPlayerCollision(player);
            }
        }
    }

    /**
     * Procesa colisiones de enemigos con elementos especiales.
     */
    public void checkEnemySpecialElementCollisions(List<Enemy> enemies,
            List<SpecialElement> specialElements) {
        for (Enemy enemy : enemies) {
            for (SpecialElement se : specialElements) {
                if (!se.isUsed() && enemy.getPosition().equals(se.getPosition())) {
                    se.onEnemyCollision(enemy);
                }
            }
        }
    }
}
