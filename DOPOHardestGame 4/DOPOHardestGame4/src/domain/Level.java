package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nivel activo del juego.
 * Recibe un LevelConfig y crea los objetos vivos: Board, Coins, Enemies, SpecialElements.
 */
import java.io.Serializable;

public class Level implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Board board;
    private final Position startPosition;
    private final Position startPosition2;
    private final List<Coin> coins;
    private final List<Enemy> enemies;
    private final List<SpecialElement> specialElements;
    private final int totalCoins;

    /**
     * Crea un nivel a partir de su configuracion.
     * @param config configuracion del nivel
     * @throws GameException si la configuracion es invalida
     */
    public Level(LevelConfig config) throws GameException {
        this.name = config.getName();
        this.startPosition = config.getStartPosition();
        this.startPosition2 = config.getStartPosition2();

        this.board = new Board(config.getGrid());

        if (!board.isWalkable(startPosition)) {
            throw new GameException("La posicion de inicio no es transitable: " + startPosition);
        }

        // Crear monedas
        this.coins = new ArrayList<>();
        for (LevelConfig.CoinConfig cc : config.getCoinConfigs()) {
            if (!board.isWalkable(cc.getPosition())) {
                throw new GameException("Moneda en posicion no transitable: " + cc.getPosition());
            }
            coins.add(createCoin(cc));
        }
        this.totalCoins = coins.size();

        // Crear enemigos
        this.enemies = new ArrayList<>();
        for (LevelConfig.EnemyConfig ec : config.getEnemyConfigs()) {
            enemies.add(createEnemy(ec));
        }

        // Crear elementos especiales
        this.specialElements = new ArrayList<>();
        for (LevelConfig.SpecialElementConfig sec : config.getSpecialElementConfigs()) {
            specialElements.add(createSpecialElement(sec));
        }

        GameLogger.info("Nivel '" + name + "' cargado: " + totalCoins
                + " monedas, " + enemies.size() + " enemigos, "
                + specialElements.size() + " elementos especiales");
    }

    // -----------------------------------------------------------------------
    // Fabricas privadas
    // -----------------------------------------------------------------------

    /**
     * Crea una moneda segun su tipo de configuracion.
     * Para agregar nuevos tipos de moneda: agregar un case aqui y en CoinType.
     */
    private Coin createCoin(LevelConfig.CoinConfig cc) throws GameException {
        switch (cc.getType()) {
            case YELLOW:    return new YellowCoin(cc.getPosition());
            case RED_SKIN:
            case BLUE_SKIN:
            case GREEN_SKIN: return new SkinCoin(cc.getPosition(), cc.getType());
            default:
                throw new GameException("Tipo de moneda desconocido: " + cc.getType());
        }
    }

    /**
     * Crea un enemigo segun su tipo de configuracion.
     * Para agregar nuevos tipos de enemigo: agregar un case aqui y en EnemyType.
     */
    private Enemy createEnemy(LevelConfig.EnemyConfig ec) throws GameException {
        switch (ec.getType()) {
            case BASIC:    return new BasicEnemy(ec.getPosition(), ec.getDirection());
            case FAST:     return new FastEnemy(ec.getPosition(), ec.getDirection());
            case PATROL:   return new PatrolEnemy(ec.getPosition(), ec.getWaypoints());
            case VERTICAL: return new VerticalEnemy(ec.getPosition(), ec.getDirection());
            default:
                throw new GameException("Tipo de enemigo desconocido: " + ec.getType());
        }
    }

    /**
     * Crea un elemento especial segun su tipo.
     * Para agregar nuevos elementos: agregar un case aqui y en SpecialElementType.
     */
    private SpecialElement createSpecialElement(LevelConfig.SpecialElementConfig sec) throws GameException {
        switch (sec.getType()) {
            case LIFE_SOURCE: return new LifeSource(sec.getPosition());
            case BOMB:        return new Bomb(sec.getPosition());
            default:
                throw new GameException("Tipo de elemento especial desconocido: " + sec.getType());
        }
    }

    // -----------------------------------------------------------------------
    // Estado del nivel
    // -----------------------------------------------------------------------

    /** Retorna true si todas las monedas han sido recolectadas. */
    public boolean allCoinsCollected() {
        for (Coin coin : coins) {
            if (!coin.isCollected()) return false;
        }
        return true;
    }

    /** Reinicia el nivel a su estado inicial. */
    public void reset() {
        for (Coin coin : coins)                     coin.reset();
        for (Enemy enemy : enemies)                 enemy.reset();
        for (SpecialElement se : specialElements)   se.reset();
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public String getName()                         { return name; }
    public Board getBoard()                         { return board; }
    public Position getStartPosition()              { return startPosition; }
    public Position getStartPosition2()             { return startPosition2; }
    public List<Coin> getCoins()                    { return coins; }
    public List<Enemy> getEnemies()                 { return enemies; }
    public List<SpecialElement> getSpecialElements(){ return specialElements; }
    public int getTotalCoins()                      { return totalCoins; }
}
