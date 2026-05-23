package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos en frio de configuracion de un nivel.
 * Contiene toda la informacion necesaria para instanciar un Level.
 *
 * Extensibilidad: para agregar nuevos tipos de elementos, agrega una nueva
 * clase de Config (CoinConfig, SpecialElementConfig, etc.) e incluye su
 * lista aqui. Level.java se encarga de instanciarlos.
 */
import java.io.Serializable;

public class LevelConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Cell[][] grid;
    private final Position startPosition;         // P1 inicio
    private final Position startPosition2;        // P2 inicio (null en modo 1 jugador)
    private final int timeLimitSeconds;
    private final List<CoinConfig> coinConfigs;
    private final List<EnemyConfig> enemyConfigs;
    private final List<SpecialElementConfig> specialElementConfigs;

    /**
     * Constructor completo.
     */
    public LevelConfig(String name, Cell[][] grid, Position startPosition,
            Position startPosition2, int timeLimitSeconds,
            List<CoinConfig> coinConfigs, List<EnemyConfig> enemyConfigs,
            List<SpecialElementConfig> specialElementConfigs) {
        this.name = name;
        this.grid = grid;
        this.startPosition = startPosition;
        this.startPosition2 = startPosition2;
        this.timeLimitSeconds = timeLimitSeconds;
        this.coinConfigs = new ArrayList<>(coinConfigs);
        this.enemyConfigs = new ArrayList<>(enemyConfigs);
        this.specialElementConfigs = new ArrayList<>(specialElementConfigs);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public String getName()                              { return name; }
    public Cell[][] getGrid()                            { return grid; }
    public Position getStartPosition()                   { return startPosition; }
    public Position getStartPosition2()                  { return startPosition2; }
    public int getTimeLimitSeconds()                     { return timeLimitSeconds; }
    public List<CoinConfig> getCoinConfigs()             { return coinConfigs; }
    public List<EnemyConfig> getEnemyConfigs()           { return enemyConfigs; }
    public List<SpecialElementConfig> getSpecialElementConfigs() { return specialElementConfigs; }

    public int getRows() { return grid.length; }
    public int getCols() { return grid[0].length; }

    // -----------------------------------------------------------------------
    // Clase interna: CoinConfig
    // -----------------------------------------------------------------------

    /**
     * Configuracion de una moneda individual dentro del nivel.
     */
    public static class CoinConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private final CoinType type;
        private final Position position;

        public CoinConfig(CoinType type, Position position) {
            this.type = type;
            this.position = position;
        }

        public CoinType getType()       { return type; }
        public Position getPosition()   { return position; }
    }

    // -----------------------------------------------------------------------
    // Clase interna: SpecialElementConfig
    // -----------------------------------------------------------------------

    /**
     * Configuracion de un elemento especial individual dentro del nivel.
     */
    public static class SpecialElementConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private final SpecialElementType type;
        private final Position position;

        public SpecialElementConfig(SpecialElementType type, Position position) {
            this.type = type;
            this.position = position;
        }

        public SpecialElementType getType()   { return type; }
        public Position getPosition()         { return position; }
    }

    /**
     * Tipos de elementos especiales disponibles.
     * Agregar nuevos tipos aqui al extender el juego.
     */
    public enum SpecialElementType {
        LIFE_SOURCE,
        BOMB
    }

    // -----------------------------------------------------------------------
    // Clase interna: EnemyConfig
    // -----------------------------------------------------------------------

    /**
     * Configuracion de un enemigo individual dentro de un nivel.
     */
    public static class EnemyConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        private final EnemyType type;
        private final Position position;
        private final Direction direction;
        private final List<Position> waypoints;

        /** Crea configuracion para enemigo lineal (BASIC, FAST, VERTICAL). */
        public EnemyConfig(EnemyType type, Position position, Direction direction) {
            this.type = type;
            this.position = position;
            this.direction = direction;
            this.waypoints = new ArrayList<>();
        }

        /** Crea configuracion para enemigo patrullero (PATROL). */
        public EnemyConfig(Position position, List<Position> waypoints) {
            this.type = EnemyType.PATROL;
            this.position = position;
            this.direction = null;
            this.waypoints = new ArrayList<>(waypoints);
        }

        public EnemyType getType()           { return type; }
        public Position getPosition()        { return position; }
        public Direction getDirection()      { return direction; }
        public List<Position> getWaypoints() { return waypoints; }
    }

    /**
     * Tipos de enemigos disponibles.
     * Agregar nuevos tipos aqui al extender el juego.
     */
    public enum EnemyType {
        BASIC,
        PATROL,
        FAST,
        VERTICAL
    }
}
