package presentation;

import domain.Player;
import domain.Game;
import domain.GameMode;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Panel superior que muestra informacion del juego:
 * Linea 1: Tiempo restante | Nombre del nivel
 * Linea 2: Info de jugadores (nombre, muertes, monedas)
 * 
 * Altura adaptativa: 50px para 1 jugador, 70px para 2 jugadores.
 */
public class HUDPanel extends JPanel {

    /** Altura del HUD en pixeles (para 2 jugadores) */
    public static final int HUD_HEIGHT = 70;

    private static final Color BG_COLOR = new Color(35, 35, 45);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color TIME_COLOR = new Color(255, 200, 50);
    private static final Color DEATH_COLOR = new Color(255, 80, 80);
    private static final Color COIN_COLOR = new Color(255, 215, 0);
    private static final Color SEPARATOR_COLOR = new Color(60, 60, 80);

    private Game game;

    public HUDPanel() {
        setPreferredSize(new Dimension(MainWindow.GAME_WIDTH, HUD_HEIGHT));
        setBackground(BG_COLOR);
    }

    /**
     * Establece la referencia al juego para obtener datos.
     */
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (game == null || game.getCurrentLevel() == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();

        // === Linea 1: Tiempo + Nivel ===
        int y1 = 22;

        // Tiempo restante (izquierda)
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        g2d.setColor(TIME_COLOR);
        String timeText = "Tiempo: " + game.getTimer().getFormattedTime();
        g2d.drawString(timeText, 15, y1);

        // Nombre del nivel (derecha del tiempo)
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.setColor(TEXT_COLOR);
        String levelName = game.getCurrentLevel().getName();
        int levelWidth = g2d.getFontMetrics().stringWidth(levelName);
        g2d.drawString(levelName, width - levelWidth - 15, y1);

        // Linea separadora
        g2d.setColor(SEPARATOR_COLOR);
        g2d.drawLine(10, 32, width - 10, 32);

        // === Linea 2: Info de jugadores ===
        int y2 = 52;
        List<Player> players = game.getPlayers();

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        if (players.size() == 1) {
            // Un solo jugador: todo en una linea centrada
            drawPlayerInfo(g2d, players.get(0), 15, y2);
        } else {
            // Dos jugadores: uno a la izquierda, otro a la derecha
            drawPlayerInfo(g2d, players.get(0), 15, y2);
            drawPlayerInfoRight(g2d, players.get(1), width - 15, y2);
        }
    }

    /**
     * Dibuja la info de un jugador alineada a la izquierda desde x.
     */
    private void drawPlayerInfo(Graphics2D g2d, Player p, int x, int y) {
        // Nombre
        g2d.setColor(p.getType().getColor());
        g2d.drawString(p.getName(), x, y);
        x += g2d.getFontMetrics().stringWidth(p.getName()) + 12;

        // Muertes
        String deathText = "Muertes: " + p.getDeaths();
        g2d.setColor(DEATH_COLOR);
        g2d.drawString(deathText, x, y);
        x += g2d.getFontMetrics().stringWidth(deathText) + 12;

        // Monedas
        String coinText = "Monedas: " + p.getCoinsCollected() + "/" + game.getCurrentLevel().getTotalCoins();
        g2d.setColor(COIN_COLOR);
        g2d.drawString(coinText, x, y);
    }

    /**
     * Dibuja la info de un jugador alineada a la derecha desde x.
     */
    private void drawPlayerInfoRight(Graphics2D g2d, Player p, int rightX, int y) {
        // Construir textos para medir
        String coinText = "Monedas: " + p.getCoinsCollected() + "/" + game.getCurrentLevel().getTotalCoins();
        String deathText = "Muertes: " + p.getDeaths();
        String nameText = p.getName();

        // Monedas (mas a la derecha)
        int coinWidth = g2d.getFontMetrics().stringWidth(coinText);
        g2d.setColor(COIN_COLOR);
        g2d.drawString(coinText, rightX - coinWidth, y);
        rightX -= coinWidth + 12;

        // Muertes
        int deathWidth = g2d.getFontMetrics().stringWidth(deathText);
        g2d.setColor(DEATH_COLOR);
        g2d.drawString(deathText, rightX - deathWidth, y);
        rightX -= deathWidth + 12;

        // Nombre
        int nameWidth = g2d.getFontMetrics().stringWidth(nameText);
        g2d.setColor(p.getType().getColor());
        g2d.drawString(nameText, rightX - nameWidth, y);
    }
}
