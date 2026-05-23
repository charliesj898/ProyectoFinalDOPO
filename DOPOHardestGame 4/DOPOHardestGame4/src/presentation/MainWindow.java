package presentation;

import domain.Game;
import domain.GameMode;
import domain.GameState;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * Ventana principal de la aplicacion.
 * Usa CardLayout para alternar entre las pantallas:
 * Menu, Configuracion, Juego.
 */
public class MainWindow extends JFrame {

    public static final int CELL_SIZE = 30;
    public static final int BOARD_COLS = 20;
    public static final int BOARD_ROWS = 15;
    public static final int GAME_WIDTH = CELL_SIZE * BOARD_COLS; // 600
    public static final int GAME_HEIGHT = CELL_SIZE * BOARD_ROWS; // 450

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private MenuPanel menuPanel;
    private ConfigPanel configPanel;
    private GamePanel gamePanel;

    public MainWindow() {
        super("The DOPO Hardest Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Crear pantallas
        menuPanel = new MenuPanel(this);
        configPanel = new ConfigPanel(this);

        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(configPanel, "CONFIG");

        add(mainPanel);

        // Dimensionar la ventana
        mainPanel.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT + HUDPanel.HUD_HEIGHT));
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Muestra la pantalla de menu principal.
     */
    public void showMenu() {
        cardLayout.show(mainPanel, "MENU");
    }

    /**
     * Muestra la pantalla de configuracion con el modo de juego preseleccionado.
     * 
     * @param mode modo de juego a preseleccionar
     */
    public void showConfig(GameMode mode) {
        configPanel.setMode(mode);
        cardLayout.show(mainPanel, "CONFIG");
    }

    /**
     * Inicia el juego con la instancia de Game dada.
     * Crea un nuevo GamePanel y lo muestra.
     */
    public void startGame(Game game) {
        // Remover GamePanel anterior si existe
        if (gamePanel != null) {
            gamePanel.stopGameLoop();
            mainPanel.remove(gamePanel);
        }

        gamePanel = new GamePanel(game, this);
        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");

        // Asegurar que el panel tenga el foco para capturar teclas
        gamePanel.requestFocusInWindow();

        // Iniciar el juego
        game.startGame();
        gamePanel.startGameLoop();
    }

    /**
     * Reanuda un juego cargado desde persistencia.
     */
    public void resumeGame(Game game) {
        if (gamePanel != null) {
            gamePanel.stopGameLoop();
            mainPanel.remove(gamePanel);
        }

        gamePanel = new GamePanel(game, this);
        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");

        gamePanel.requestFocusInWindow();

        // Iniciar solo el loop de la vista, no reiniciamos el modelo (el nivel)
        gamePanel.startGameLoop();
    }

    /**
     * Punto de entrada de la aplicacion.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            domain.GameLogger.init();
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
