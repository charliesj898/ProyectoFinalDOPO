package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador central del juego.
 *
 * Cambios respecto a la version anterior:
 * - Recibe AIProfile para instanciar el controlador de IA correcto.
 * - Cada jugador tiene una goalCell (SAFE_FINAL para P1, SAFE_FINAL_2 para P2).
 * - Detecta colision jugador vs jugador en modos de 2 jugadores.
 * - Procesa colisiones con elementos especiales.
 * - El tiempo del nivel proviene de LevelConfig.
 */
import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<LevelConfig> levelConfigs;
    private final List<Player> players;
    private final GameMode mode;
    private final GameTimer timer;
    private final CollisionDetector collisionDetector;
    private final MovementEngine movementEngine;
    private final AIController aiController;
    private final AIProfile aiProfile;

    private Level currentLevel;
    private int currentLevelIndex;
    private GameState state;
    private transient GameListener listener;

    /**
     * Crea una nueva partida.
     * @param mode         modo de juego
     * @param players      lista de jugadores (1 o 2)
     * @param levelConfigs lista de configuraciones de niveles
     * @param aiProfile    perfil de IA (solo relevante en PvsM; puede ser null)
     */
    public Game(GameMode mode, List<Player> players, List<LevelConfig> levelConfigs,
            AIProfile aiProfile) {
        this.mode = mode;
        this.players = new ArrayList<>(players);
        this.levelConfigs = new ArrayList<>(levelConfigs);
        this.timer = new GameTimer();
        this.collisionDetector = new CollisionDetector();
        this.movementEngine = new MovementEngine();
        this.aiController = (aiProfile != null)
                ? AIController.create(aiProfile)
                : AIController.create(AIProfile.EXPERT);
        this.aiProfile = aiProfile;
        this.currentLevelIndex = 0;
        this.state = GameState.MENU;

        // Asignar celdas objetivo segun el modo de juego
        assignGoalCells();
    }

    /**
     * Asigna la goalCell correcta a cada jugador segun el modo.
     * P1 siempre busca SAFE_FINAL; P2 (en modos de 2 jugadores) busca SAFE_FINAL_2.
     */
    private void assignGoalCells() {
        if (!players.isEmpty()) {
            players.get(0).setGoalCell(Cell.SAFE_FINAL); // P1: sale de START, llega a SAFE_FINAL
        }
        if (players.size() > 1 &&
                (mode == GameMode.PLAYER_VS_PLAYER || mode == GameMode.PLAYER_VS_MACHINE)) {
            players.get(1).setGoalCell(Cell.START); // P2: sale de SAFE_FINAL, llega a START
        }
    }

    // -----------------------------------------------------------------------
    // Control del juego
    // -----------------------------------------------------------------------

    public void startGame() {
        try {
            currentLevelIndex = 0;
            loadLevel(currentLevelIndex);
            state = GameState.PLAYING;
            timer.start();
            GameLogger.info("Juego iniciado en modo: " + mode);
        } catch (GameException e) {
            GameLogger.error("Error al iniciar el juego", e);
            state = GameState.GAME_OVER;
        }
    }

    public void pause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            timer.pause();
            notifyStateChanged();
        }
    }

    public void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            timer.resume();
            notifyStateChanged();
        }
    }

    public void quit() {
        timer.stop();
        state = GameState.MENU;
        notifyStateChanged();
    }

    public void nextLevel() {
        currentLevelIndex++;
        if (currentLevelIndex >= levelConfigs.size()) {
            state = GameState.VICTORY;
            timer.stop();
            GameLogger.info("Victoria! Todos los niveles completados");
            notifyStateChanged();
            return;
        }
        try {
            loadLevel(currentLevelIndex);
            state = GameState.PLAYING;
            timer.start();
            notifyStateChanged();
        } catch (GameException e) {
            GameLogger.error("Error al cargar nivel " + currentLevelIndex, e);
            state = GameState.GAME_OVER;
            notifyStateChanged();
        }
    }

    // -----------------------------------------------------------------------
    // Game loop
    // -----------------------------------------------------------------------

    public void tick() {
        if (state != GameState.PLAYING) return;

        // 1. Tiempo agotado
        if (timer.isExpired()) {
            state = GameState.GAME_OVER;
            timer.stop();
            GameLogger.info("Tiempo agotado en nivel " + currentLevelIndex);
            notifyStateChanged();
            return;
        }

        // 2. Cooldowns de movimiento
        for (Player player : players) {
            player.tick();
        }

        // 3. IA
        if (mode == GameMode.PLAYER_VS_MACHINE && players.size() > 1) {
            Player aiPlayer = players.get(1);
            Direction aiDirection = aiController.getNextDirection(aiPlayer, currentLevel);
            if (aiDirection != null) {
                aiPlayer.tryMove(aiDirection, currentLevel.getBoard());
            }
        }

        // 4. Mover enemigos
        movementEngine.update(currentLevel.getEnemies(), currentLevel.getBoard());

        // 5. Colision enemigos con elementos especiales
        collisionDetector.checkEnemySpecialElementCollisions(
                currentLevel.getEnemies(), currentLevel.getSpecialElements());

        // 6. Colision jugador vs jugador (modos de 2 jugadores)
        if (players.size() > 1 &&
                (mode == GameMode.PLAYER_VS_PLAYER || mode == GameMode.PLAYER_VS_MACHINE)) {
            if (state == GameState.PLAYING &&
                    collisionDetector.checkPlayerPlayerCollision(players.get(0), players.get(1))) {
                GameLogger.info("Colision entre jugadores!");
                // Ambos jugadores mueren
                players.get(0).handleEnemyCollision();
                players.get(1).handleEnemyCollision();
            }
        }

        // 7. Colisiones por jugador
        for (Player player : players) {
            if (state == GameState.PLAYING) {
                processCollisions(player);
            }
        }
    }

    private void processCollisions(Player player) {
        // vs. Enemigo
        if (collisionDetector.checkPlayerEnemyCollision(player, currentLevel.getEnemies())) {
            boolean died = player.handleEnemyCollision();
            if (died) {
                GameLogger.info(player.getName() + " murio. Muertes: " + player.getDeaths());
                for (Enemy enemy : currentLevel.getEnemies()) {
                    enemy.reset();
                }
            }
        }

        // vs. Moneda
        collisionDetector.checkPlayerCoinCollision(player, currentLevel.getCoins());

        // vs. Elementos especiales
        collisionDetector.checkPlayerSpecialElementCollisions(
                player, currentLevel.getSpecialElements());

        // vs. Zona segura
        Cell safeZone = collisionDetector.checkPlayerSafeZone(player, currentLevel.getBoard());
        if (safeZone == player.getGoalCell() && currentLevel.allCoinsCollected()) {
            if (!player.hasReachedGoal()) {
                player.setReachedGoal(true);
                player.setLevelGoalReachedTimeMs(timer.getElapsedMillis());
            }

            /*
             * Condicion de fin de nivel:
             * - SINGLE_PLAYER: el unico jugador llego a su meta.
             * - PvsP / PvsM:  es una carrera — el nivel termina cuando
             *   CUALQUIER jugador llega a SU zona final (primero en llegar, gana).
             */
            boolean levelComplete;
            if (mode == GameMode.SINGLE_PLAYER) {
                // Con 1 solo jugador, allReached siempre sera true aqui
                levelComplete = true;
            } else {
                // En modos de 2 jugadores, el jugador actual acaba de llegar -> termina
                levelComplete = player.hasReachedGoal();
            }

            if (levelComplete) {
                player.addLevelWin();
                for (Player p : players) p.addLevelTime();
                state = GameState.LEVEL_COMPLETE;
                timer.stop();
                GameLogger.info("Nivel " + currentLevelIndex + " completado por " + player.getName());
                notifyStateChanged();
            }
        } else if (safeZone != player.getGoalCell()) {
            player.setReachedGoal(false);
        }
    }

    // -----------------------------------------------------------------------
    // Movimiento de jugadores
    // -----------------------------------------------------------------------

    public void movePlayer(int playerIndex, Direction direction) {
        if (state != GameState.PLAYING) return;
        if (playerIndex >= 0 && playerIndex < players.size()) {
            players.get(playerIndex).tryMove(direction, currentLevel.getBoard());
        }
    }

    // -----------------------------------------------------------------------
    // Carga de niveles
    // -----------------------------------------------------------------------

    private void loadLevel(int index) throws GameException {
        if (index < 0 || index >= levelConfigs.size()) {
            throw new GameException("Indice de nivel invalido: " + index);
        }

        LevelConfig config = levelConfigs.get(index);
        currentLevel = new Level(config);

        // Reiniciar timer con el tiempo del nivel
        timer.setTimeLimit(config.getTimeLimitSeconds());

        // P1 siempre en startPosition
        players.get(0).resetForLevel(currentLevel.getStartPosition());

        // P2 en startPosition2 (si existe), sino en startPosition
        if (players.size() > 1) {
            Position p2Start = currentLevel.getStartPosition2() != null
                    ? currentLevel.getStartPosition2()
                    : currentLevel.getStartPosition();
            players.get(1).resetForLevel(p2Start);
        }
    }

    // -----------------------------------------------------------------------
    // GameListener
    // -----------------------------------------------------------------------

    public interface GameListener {
        void onStateChanged(GameState newState);
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    private void notifyStateChanged() {
        if (listener != null) listener.onStateChanged(state);
    }

    /**
     * Define el estado actual del juego.
     */
    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Carga el nivel especificado de forma limpia para una restauración de partida.
     */
    public void loadLevelForRestore(int index) throws GameException {
        if (index < 0 || index >= levelConfigs.size()) {
            throw new GameException("Indice de nivel invalido: " + index);
        }
        this.currentLevelIndex = index;
        LevelConfig config = levelConfigs.get(index);
        this.currentLevel = new Level(config);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public GameState getState()       { return state; }
    public GameMode getMode()         { return mode; }
    public Level getCurrentLevel()    { return currentLevel; }
    public int getCurrentLevelIndex() { return currentLevelIndex; }
    public int getTotalLevels()       { return levelConfigs.size(); }
    public List<Player> getPlayers()  { return players; }
    public GameTimer getTimer()       { return timer; }
    public AIProfile getAIProfile()   { return aiProfile; }

    /**
     * Determina el ganador en modos multijugador.
     * Criterio 1: Mas niveles ganados (llego primero).
     * Criterio 2 (desempate): Menos muertes.
     * Criterio 3 (desempate): Menor tiempo total.
     */
    public Player getWinner() {
        if (mode == GameMode.SINGLE_PLAYER || players.size() < 2) return null;
        Player p1 = players.get(0);
        Player p2 = players.get(1);
        if (p1.getLevelWins() > p2.getLevelWins()) return p1;
        if (p2.getLevelWins() > p1.getLevelWins()) return p2;
        if (p1.getDeaths() < p2.getDeaths()) return p1;
        if (p2.getDeaths() < p1.getDeaths()) return p2;
        if (p1.getTotalGoalReachedTimeMs() < p2.getTotalGoalReachedTimeMs()) return p1;
        if (p2.getTotalGoalReachedTimeMs() < p1.getTotalGoalReachedTimeMs()) return p2;
        return null;
    }
}
