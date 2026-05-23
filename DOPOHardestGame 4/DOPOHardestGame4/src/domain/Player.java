package domain;

import java.awt.Color;

/**
 * Representa un personaje controlable en el juego.
 *
 * Novedades respecto a la version anterior:
 * - Skin temporal: al recolectar una SkinCoin, el jugador adopta otro PlayerType
 *   hasta morir o recolectar otra moneda.
 * - Escudo activable: LifeSource puede activar el escudo igual que el tipo GREEN.
 * - Color de borde personalizable.
 * - goalCell: la celda que este jugador debe alcanzar para completar el nivel.
 */
import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final PlayerType originalType;  // Tipo original (inmutable)
    private PlayerType activeType;          // Tipo en uso (puede cambiar con skin coins)

    private Position position;
    private Position respawnPoint;
    private int deaths;
    private float speed;
    private boolean shieldActive;
    private int moveCooldown;
    private int ticksSinceLastMove;
    private int coinsCollected;
    private boolean invincible;
    private int invincibilityTicks;
    private boolean reachedGoal;
    private long levelGoalReachedTimeMs;
    private long totalGoalReachedTimeMs;
    private int levelWins; // Cantidad de niveles ganados (llego primero a su meta)

    /** Color del borde del cuadrado (personalizable). */
    private Color borderColor;

    /**
     * Celda que este jugador debe alcanzar (con monedas) para completar el nivel.
     * P1 -> SAFE_FINAL, P2 en modos de 2 jugadores -> SAFE_FINAL_2.
     */
    private Cell goalCell;

    private static final int BASE_MOVE_COOLDOWN = 4;
    private static final int INVINCIBILITY_DURATION = 30;

    /**
     * Crea un nuevo jugador.
     * @param name          nombre del jugador
     * @param type          tipo de jugador
     * @param startPosition posicion inicial
     */
    public Player(String name, PlayerType type, Position startPosition) {
        this.name = name;
        this.originalType = type;
        this.activeType = type;
        this.position = startPosition;
        this.respawnPoint = startPosition;
        this.deaths = 0;
        this.coinsCollected = 0;
        this.ticksSinceLastMove = BASE_MOVE_COOLDOWN;
        this.speed = type.getBaseSpeed();
        this.shieldActive = type.hasShield();
        this.moveCooldown = calculateMoveCooldown(this.speed);
        this.borderColor = type.getColor().darker();
        this.goalCell = Cell.SAFE_FINAL; // Por defecto P1
    }

    // -----------------------------------------------------------------------
    // Logica de movimiento
    // -----------------------------------------------------------------------

    private int calculateMoveCooldown(float spd) {
        return Math.max(1, Math.round(BASE_MOVE_COOLDOWN / spd));
    }

    public boolean canMove() {
        return ticksSinceLastMove >= moveCooldown;
    }

    public void tick() {
        ticksSinceLastMove++;
        if (invincibilityTicks > 0) {
            invincibilityTicks--;
            if (invincibilityTicks <= 0) {
                invincible = false;
            }
        }
    }

    /**
     * Intenta mover al jugador en la direccion dada.
     * Soporta direcciones cardinales y diagonales.
     */
    public boolean tryMove(Direction direction, Board board) {
        if (!canMove()) {
            return false;
        }
        Position newPos = position.move(direction);
        if (board.isWalkable(newPos)) {
            position = newPos;
            ticksSinceLastMove = 0;
            return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Colisiones y escudo
    // -----------------------------------------------------------------------

    /**
     * Maneja colision con un enemigo.
     * Si el jugador tiene escudo (GREEN o LifeSource), absorbe el golpe.
     * @return true si el jugador murio
     */
    public boolean handleEnemyCollision() {
        if (invincible) {
            return false;
        }
        if (shieldActive) {
            shieldActive = false;
            speed = 0.7f;
            moveCooldown = calculateMoveCooldown(speed);
            setInvincible();
            return false;
        }
        die();
        return true;
    }

    /**
     * Colision con bomba: mata al jugador ignorando el escudo.
     */
    public void handleBombCollision() {
        if (invincible) return;
        die();
    }

    private void die() {
        deaths++;
        position = respawnPoint;
        reachedGoal = false;
        levelGoalReachedTimeMs = 0;
        resetSkin();
        speed = activeType.getBaseSpeed();
        moveCooldown = calculateMoveCooldown(speed);
        if (originalType.hasShield()) {
            shieldActive = true;
        } else {
            shieldActive = false;
        }
        setInvincible();
    }

    private void setInvincible() {
        invincible = true;
        invincibilityTicks = INVINCIBILITY_DURATION;
    }

    /**
     * Activa el escudo de absorcion (desde LifeSource u otro elemento).
     */
    public void activateShield() {
        this.shieldActive = true;
    }

    // -----------------------------------------------------------------------
    // Sistema de skin temporal
    // -----------------------------------------------------------------------

    /**
     * Aplica el efecto de una skin coin: cambia el tipo activo temporalmente.
     * Adapta las habilidades del nuevo color (velocidad, escudo, etc).
     * @param skinType tipo de jugador a aplicar
     */
    public void applySkinEffect(PlayerType skinType) {
        this.activeType = skinType;
        // Ajustar velocidad y habilidades al nuevo tipo
        this.speed = skinType.getBaseSpeed();
        this.moveCooldown = calculateMoveCooldown(speed);
        this.shieldActive = skinType.hasShield();
    }

    /**
     * Restaura el tipo activo al tipo original, recuperando sus habilidades base.
     * Se llama al morir o al reiniciar el nivel.
     */
    public void resetSkin() {
        if (activeType != originalType) {
            activeType = originalType;
            speed = originalType.getBaseSpeed();
            moveCooldown = calculateMoveCooldown(speed);
            shieldActive = originalType.hasShield();
        }
    }

    // -----------------------------------------------------------------------
    // Zona segura y objetivo
    // -----------------------------------------------------------------------

    public void updateRespawnPoint(Position newRespawn) {
        this.respawnPoint = newRespawn;
    }

    public void collectCoin() {
        coinsCollected++;
    }

    public void resetForLevel(Position startPosition) {
        this.position = startPosition;
        this.respawnPoint = startPosition;
        this.coinsCollected = 0;
        this.speed = originalType.getBaseSpeed();
        this.moveCooldown = calculateMoveCooldown(speed);
        this.shieldActive = originalType.hasShield();
        this.ticksSinceLastMove = BASE_MOVE_COOLDOWN;
        this.invincible = false;
        this.invincibilityTicks = 0;
        this.reachedGoal = false;
        this.levelGoalReachedTimeMs = 0;
        resetSkin();
    }

    /**
     * Restaura el estado completo del jugador a partir de datos cargados de una partida.
     */
    public void restoreState(Position position, Position respawnPoint, int deaths, float speed, boolean shieldActive,
            int coinsCollected, boolean invincible, int invincibilityTicks, boolean reachedGoal,
            long levelGoalReachedTimeMs, long totalGoalReachedTimeMs, int levelWins, Color borderColor, Cell goalCell, PlayerType activeType) {
        this.position = position;
        this.respawnPoint = respawnPoint;
        this.deaths = deaths;
        this.speed = speed;
        this.shieldActive = shieldActive;
        this.coinsCollected = coinsCollected;
        this.invincible = invincible;
        this.invincibilityTicks = invincibilityTicks;
        this.reachedGoal = reachedGoal;
        this.levelGoalReachedTimeMs = levelGoalReachedTimeMs;
        this.totalGoalReachedTimeMs = totalGoalReachedTimeMs;
        this.levelWins = levelWins;
        this.borderColor = borderColor;
        this.goalCell = goalCell;
        this.activeType = activeType;
        this.moveCooldown = calculateMoveCooldown(speed);
        this.ticksSinceLastMove = BASE_MOVE_COOLDOWN;
    }

    // -----------------------------------------------------------------------
    // Getters y setters
    // -----------------------------------------------------------------------

    public String getName()             { return name; }
    public PlayerType getType()         { return activeType; }
    public PlayerType getOriginalType() { return originalType; }
    public Position getPosition()       { return position; }
    public Position getRespawnPoint()   { return respawnPoint; }
    public int getDeaths()              { return deaths; }
    public float getSpeed()             { return speed; }
    public boolean isShieldActive()     { return shieldActive; }
    public boolean isInvincible()       { return invincible; }
    public boolean hasReachedGoal()     { return reachedGoal; }
    public int getCoinsCollected()      { return coinsCollected; }
    public Cell getGoalCell()           { return goalCell; }

    public void setGoalCell(Cell goalCell) {
        this.goalCell = goalCell;
    }

    public void setReachedGoal(boolean reachedGoal) {
        this.reachedGoal = reachedGoal;
    }

    public long getLevelGoalReachedTimeMs()  { return levelGoalReachedTimeMs; }
    public long getTotalGoalReachedTimeMs()  { return totalGoalReachedTimeMs; }

    public void setLevelGoalReachedTimeMs(long ms) {
        this.levelGoalReachedTimeMs = ms;
    }

    public void addLevelTime() {
        this.totalGoalReachedTimeMs += this.levelGoalReachedTimeMs;
    }

    /**
     * Incrementa el contador de victorias de nivel.
     * Se llama cuando este jugador es el primero en llegar a su meta.
     */
    public void addLevelWin() {
        this.levelWins++;
    }

    public int getLevelWins() {
        return levelWins;
    }

    public Color getBorderColor()              { return borderColor; }
    public void setBorderColor(Color color)    { this.borderColor = color; }

    @Override
    public String toString() {
        return name + " [" + activeType + "] en " + position + " (muertes: " + deaths + ")";
    }
}
