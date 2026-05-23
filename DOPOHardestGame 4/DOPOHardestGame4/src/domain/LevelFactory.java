package domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Clase de fabrica que crea las configuraciones de los 3 niveles predefinidos.
 *
 * Tablero: 20 columnas x 15 filas, celdas de 30x30 px.
 *
 * Leyenda de las grillas (int):
 *   0 = EMPTY
 *   1 = WALL
 *   2 = START      (inicio P1 / meta P2 en modos 2 jugadores)
 *   3 = SAFE_INTERMEDIATE
 *   4 = SAFE_FINAL (meta P1 / inicio P2 en modos 2 jugadores)
 *   5 = SAFE_FINAL_2 (actualmente no usado en hardcode; P2 usa SAFE_FINAL del mapa opuesto)
 */
public class LevelFactory {

    /**
     * Crea y retorna la lista de configuraciones de los niveles cargados desde texto.
     */
    public static List<LevelConfig> createAllLevels() {
        List<LevelConfig> levels = new ArrayList<>();
        try {
            levels.add(LevelLoader.loadLevel("levels/level1.txt", "Nivel 1 - Tutorial"));
            levels.add(LevelLoader.loadLevel("levels/level2.txt", "Nivel 2 - Intermedio"));
            levels.add(LevelLoader.loadLevel("levels/level3.txt", "Nivel 3 - Dificil"));
        } catch (GameException e) {
            GameLogger.error("Error cargando niveles desde .txt", e);
        }
        return levels;
    }
}
