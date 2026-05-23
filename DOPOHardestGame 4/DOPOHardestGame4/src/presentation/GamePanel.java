package presentation;

import domain.Coin;
import domain.Enemy;
import domain.Game;
import domain.GameMode;
import domain.GameState;
import domain.LevelConfig;
import domain.Player;
import domain.SpecialElement;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Panel principal del juego que contiene:
 * - El HUD (arriba) con informacion del juego
 * - El area de juego (abajo) con el tablero y entidades
 * - El game loop usando javax.swing.Timer
 * 
 * El game loop se ejecuta cada ~16ms (~60 FPS).
 * En cada tick:
 * 1. Procesa input del teclado
 * 2. Ejecuta game.tick()
 * 3. Repinta la pantalla
 */
public class GamePanel extends JPanel {

    private static final int TICK_DELAY = 16; // ~60 FPS
    private static final Color BG_COLOR = new Color(30, 30, 40);

    private final Game game;
    private final MainWindow mainWindow;
    private final HUDPanel hudPanel;
    private final GameArea gameArea;
    private final InputHandler inputHandler;
    private Timer gameLoopTimer;

    public GamePanel(Game game, MainWindow mainWindow) {
        this.game = game;
        this.mainWindow = mainWindow;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // HUD (parte superior)
        hudPanel = new HUDPanel();
        hudPanel.setGame(game);
        add(hudPanel, BorderLayout.NORTH);

        // Area de juego (parte central)
        gameArea = new GameArea();
        add(gameArea, BorderLayout.CENTER);

        // Input handler
        inputHandler = new InputHandler(game, this);
        addKeyListener(inputHandler);
        setFocusable(true);

        // Listener para cambios de estado
        game.setGameListener(this::onGameStateChanged);
    }

    /**
     * Inicia el game loop.
     */
    public void startGameLoop() {
        if (gameLoopTimer != null) {
            gameLoopTimer.stop();
        }

        gameLoopTimer = new Timer(TICK_DELAY, e -> {
            // 1. Procesar input
            inputHandler.processInput();

            // 2. Actualizar logica del juego
            game.tick();

            // 3. Avanzar frame de animacion y repintar
            GameRenderer.nextFrame();
            hudPanel.repaint();
            gameArea.repaint();
        });

        gameLoopTimer.start();
    }

    /**
     * Detiene el game loop.
     */
    public void stopGameLoop() {
        if (gameLoopTimer != null) {
            gameLoopTimer.stop();
        }
    }

    /**
     * Alterna pausa/reanudacion del juego.
     */
    public void togglePause() {
        if (game.getState() == GameState.PLAYING) {
            game.pause();
            gameArea.repaint();
        } else if (game.getState() == GameState.PAUSED) {
            game.resume();
        }
    }

    /**
     * Sale al menu principal.
     */
    public void exitToMenu() {
        stopGameLoop();
        game.quit();
        mainWindow.showMenu();
    }

    /**
     * Callback cuando el estado del juego cambia.
     */
    private void onGameStateChanged(GameState newState) {
        switch (newState) {
            case LEVEL_COMPLETE:
                stopGameLoop();
                gameArea.repaint();
                // Mostrar dialogo despues de un breve delay para que se vea el estado final
                Timer delayTimer = new Timer(500, e -> {
                    ((Timer) e.getSource()).stop();
                    showLevelCompleteDialog();
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
                break;

            case GAME_OVER:
                stopGameLoop();
                gameArea.repaint();
                Timer gameOverTimer = new Timer(500, e -> {
                    ((Timer) e.getSource()).stop();
                    showGameOverDialog();
                });
                gameOverTimer.setRepeats(false);
                gameOverTimer.start();
                break;

            case VICTORY:
                stopGameLoop();
                gameArea.repaint();
                Timer victoryTimer = new Timer(500, e -> {
                    ((Timer) e.getSource()).stop();
                    showVictoryDialog();
                });
                victoryTimer.setRepeats(false);
                victoryTimer.start();
                break;

            default:
                break;
        }
    }

    private void showLevelCompleteDialog() {
        StringBuilder message = new StringBuilder();
        message.append("Nivel completado!\n\n");

        // Mostrar stats de cada jugador
        for (Player p : game.getPlayers()) {
            double seconds = p.getLevelGoalReachedTimeMs() / 1000.0;
            message.append(p.getName()).append(": ")
                   .append(p.getDeaths()).append(" muertes");
            if (p.getLevelGoalReachedTimeMs() > 0) {
                message.append(", tiempo: ").append(String.format("%.2fs", seconds));
            }
            message.append("\n");
        }

        // En modos de 2 jugadores, indicar quien gano ESTE nivel (llego primero)
        if (game.getMode() != GameMode.SINGLE_PLAYER && game.getPlayers().size() > 1) {
            Player levelWinner = null;
            long bestTime = Long.MAX_VALUE;
            for (Player p : game.getPlayers()) {
                long t = p.getLevelGoalReachedTimeMs();
                if (t > 0 && t < bestTime) {
                    bestTime = t;
                    levelWinner = p;
                }
            }
            if (levelWinner != null) {
                message.append("\nGano el nivel: ").append(levelWinner.getName()).append("!");
            }
        }

        int option = JOptionPane.showOptionDialog(this,
                message.toString(),
                "Nivel Completado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Siguiente Nivel", "Salir al Menu"},
                "Siguiente Nivel");

        if (option == 0) {
            game.nextLevel();
            if (game.getState() == GameState.PLAYING) {
                hudPanel.repaint();
                startGameLoop();
                requestFocusInWindow();
            }
        } else {
            exitToMenu();
        }
    }

    private void showGameOverDialog() {
        StringBuilder message = new StringBuilder();
        message.append("Tiempo agotado!\n\n");
        for (Player p : game.getPlayers()) {
            message.append(p.getName()).append(": ")
                   .append(p.getDeaths()).append(" muertes\n");
        }

        JOptionPane.showMessageDialog(this, message.toString(),
                "Game Over", JOptionPane.WARNING_MESSAGE);
        exitToMenu();
    }

    private void showVictoryDialog() {
        StringBuilder message = new StringBuilder();
        message.append("Felicidades! Has completado todos los niveles!\n\n");
        for (Player p : game.getPlayers()) {
            double seconds = p.getTotalGoalReachedTimeMs() / 1000.0;
            message.append(p.getName()).append(": ")
                   .append(p.getLevelWins()).append(" niveles ganados, ")
                   .append(p.getDeaths()).append(" muertes totales, tiempo total: ")
                   .append(String.format("%.2fs", seconds)).append("\n");
        }

        // Mostrar ganador en modos multijugador
        Player winner = game.getWinner();
        if (winner != null) {
            double winnerSeconds = winner.getTotalGoalReachedTimeMs() / 1000.0;
            message.append("\nGanador: ").append(winner.getName())
                   .append(" con ").append(winner.getLevelWins()).append(" niveles ganados (")
                   .append(winner.getDeaths()).append(" muertes y ")
                   .append(String.format("%.2fs", winnerSeconds)).append(")!");
        } else if (game.getPlayers().size() > 1) {
            message.append("\nEmpate absoluto! Ambos tuvieron las mismas muertes y el mismo tiempo exacto.");
        }

        JOptionPane.showMessageDialog(this, message.toString(),
                "Victoria!", JOptionPane.INFORMATION_MESSAGE);
        exitToMenu();
    }

    // -----------------------------------------------------------------------
    // Clase interna: area de renderizado del juego
    // -----------------------------------------------------------------------

    /**
     * Panel interno que renderiza el tablero y las entidades del juego.
     */
    private class GameArea extends JPanel {

        public GameArea() {
            setPreferredSize(new Dimension(MainWindow.GAME_WIDTH, MainWindow.GAME_HEIGHT));
            setBackground(BG_COLOR);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (game.getCurrentLevel() == null) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            GameRenderer.enableAntialiasing(g2d);

            int offsetY = 0; // Sin offset ya que el HUD esta en un panel separado

            // 1. Dibujar tablero
            GameRenderer.drawBoard(g2d, game.getCurrentLevel().getBoard(), offsetY);

            // 2. Dibujar monedas
            for (Coin coin : game.getCurrentLevel().getCoins()) {
                GameRenderer.drawCoin(g2d, coin, offsetY);
            }

            // 3. Dibujar elementos especiales
            for (SpecialElement se : game.getCurrentLevel().getSpecialElements()) {
                GameRenderer.drawSpecialElement(g2d, se, offsetY);
            }

            // 4. Dibujar enemigos
            for (Enemy enemy : game.getCurrentLevel().getEnemies()) {
                GameRenderer.drawEnemy(g2d, enemy, offsetY);
            }

            // 4. Dibujar jugadores
            java.util.List<Player> players = game.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                GameRenderer.drawPlayer(g2d, players.get(i), i + 1, offsetY);
            }

            // 5. Overlays de estado
            if (game.getState() == GameState.PAUSED) {
                GameRenderer.drawCenteredMessage(g2d, "PAUSA",
                        "Presiona P para continuar | G para guardar | ESC para salir",
                        getWidth(), getHeight());
            } else if (game.getState() == GameState.LEVEL_COMPLETE) {
                GameRenderer.drawCenteredMessage(g2d, "NIVEL COMPLETADO!", null,
                        getWidth(), getHeight());
            } else if (game.getState() == GameState.GAME_OVER) {
                GameRenderer.drawCenteredMessage(g2d, "GAME OVER",
                        "Tiempo agotado", getWidth(), getHeight());
            } else if (game.getState() == GameState.VICTORY) {
                GameRenderer.drawCenteredMessage(g2d, "VICTORIA!",
                        "Completaste todos los niveles", getWidth(), getHeight());
            }
        }
    }
}
