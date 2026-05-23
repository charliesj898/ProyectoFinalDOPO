package presentation;

import domain.Board;
import domain.Cell;
import domain.Coin;
import domain.CoinType;
import domain.Enemy;
import domain.BasicEnemy;
import domain.PatrolEnemy;
import domain.FastEnemy;
import domain.VerticalEnemy;
import domain.SpecialElement;
import domain.LifeSource;
import domain.Bomb;
import domain.Player;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Clase auxiliar con metodos estaticos para dibujar todos los elementos del juego.
 * Para agregar la representacion visual de un nuevo tipo de entidad:
 * - Crea un metodo drawXxx() aqui o agrega un case/instanceof a los metodos existentes.
 */
public class GameRenderer {

    public static final int CELL_SIZE = MainWindow.CELL_SIZE;

    // Colores del tablero
    private static final Color COLOR_EMPTY       = new Color(220, 220, 230);
    private static final Color COLOR_WALL        = new Color(50, 50, 60);
    private static final Color COLOR_START       = new Color(144, 238, 144);
    private static final Color COLOR_SAFE_INTER  = new Color(100, 200, 100);
    private static final Color COLOR_SAFE_FINAL  = new Color(50, 180, 50);
    private static final Color COLOR_SAFE_FINAL2 = new Color(80, 180, 220); // Zona P2: azul-verde
    private static final Color COLOR_GRID_LINE   = new Color(200, 200, 210);

    // Colores de enemigos
    private static final Color COLOR_ENEMY_BASIC   = new Color(60, 100, 220);
    private static final Color COLOR_ENEMY_PATROL  = new Color(80, 60, 200);
    private static final Color COLOR_ENEMY_FAST    = new Color(30, 60, 180);
    private static final Color COLOR_ENEMY_VERTICAL= new Color(40, 140, 210);

    // Colores de monedas skin
    private static final Color COLOR_COIN_YELLOW = new Color(255, 215, 0);
    private static final Color COLOR_COIN_RED    = new Color(220, 80, 80);
    private static final Color COLOR_COIN_BLUE   = new Color(80, 120, 255);
    private static final Color COLOR_COIN_GREEN  = new Color(80, 200, 80);

    // -----------------------------------------------------------------------
    // Tablero
    // -----------------------------------------------------------------------

    public static void enableAntialiasing(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void drawBoard(Graphics2D g2d, Board board, int offsetY) {
        Cell[][] grid = board.getGrid();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE + offsetY;
                g2d.setColor(getCellColor(grid[r][c]));
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                if (grid[r][c] != Cell.WALL) {
                    g2d.setColor(COLOR_GRID_LINE);
                    g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Jugadores
    // -----------------------------------------------------------------------

    private static int frameCounter = 0;

    public static void nextFrame() { frameCounter++; }

    /**
     * Dibuja un jugador con su color activo (skin), borde personalizado y etiqueta P1/P2.
     */
    public static void drawPlayer(Graphics2D g2d, Player player, int playerNum, int offsetY) {
        if (player.isInvincible() && frameCounter % 4 < 2) return;

        int size = player.getType().getSize();
        int x = player.getPosition().getCol() * CELL_SIZE + (CELL_SIZE - size) / 2;
        int y = player.getPosition().getRow() * CELL_SIZE + (CELL_SIZE - size) / 2 + offsetY;

        // Cuerpo
        g2d.setColor(player.getType().getColor());
        g2d.fillRect(x, y, size, size);

        // Borde personalizado
        g2d.setColor(player.getBorderColor());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, size, size);
        g2d.setStroke(new BasicStroke(1));

        // Etiqueta P1/P2
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String text = "P" + playerNum;
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + (size - textWidth) / 2, y + (size + 8) / 2);

        // Indicador de escudo activo
        if (player.isShieldActive()) {
            g2d.setColor(new Color(100, 255, 100, 120));
            int shieldSize = size + 6;
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x - 3, y - 3, shieldSize, shieldSize, 4, 4);
            g2d.setStroke(new BasicStroke(1));
        }
    }

    // -----------------------------------------------------------------------
    // Monedas
    // -----------------------------------------------------------------------

    public static void drawCoin(Graphics2D g2d, Coin coin, int offsetY) {
        if (coin.isCollected()) return;

        int coinSize = 14;
        int x = coin.getPosition().getCol() * CELL_SIZE + (CELL_SIZE - coinSize) / 2;
        int y = coin.getPosition().getRow() * CELL_SIZE + (CELL_SIZE - coinSize) / 2 + offsetY;

        Color coinColor = getCoinColor(coin.getType());
        g2d.setColor(coinColor);
        g2d.fillOval(x, y, coinSize, coinSize);

        g2d.setColor(coinColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y, coinSize, coinSize);
        g2d.setStroke(new BasicStroke(1));

        // Letra identificadora para skin coins
        if (coin.getType() != CoinType.YELLOW) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 9));
            String letter = coin.getType().name().substring(0, 1); // R, B, G
            int lw = g2d.getFontMetrics().stringWidth(letter);
            g2d.drawString(letter, x + (coinSize - lw) / 2, y + (coinSize + 7) / 2);
        }
    }

    // -----------------------------------------------------------------------
    // Enemigos
    // -----------------------------------------------------------------------

    public static void drawEnemy(Graphics2D g2d, Enemy enemy, int offsetY) {
        int enemySize = 20;
        int x = enemy.getPosition().getCol() * CELL_SIZE + (CELL_SIZE - enemySize) / 2;
        int y = enemy.getPosition().getRow() * CELL_SIZE + (CELL_SIZE - enemySize) / 2 + offsetY;

        Color baseColor;
        if (enemy instanceof FastEnemy) {
            baseColor = COLOR_ENEMY_FAST;
        } else if (enemy instanceof PatrolEnemy) {
            baseColor = COLOR_ENEMY_PATROL;
        } else if (enemy instanceof VerticalEnemy) {
            baseColor = COLOR_ENEMY_VERTICAL;
        } else {
            baseColor = COLOR_ENEMY_BASIC;
        }

        // Forma: VerticalEnemy -> oval alargado vertical; resto -> circulo
        if (enemy instanceof VerticalEnemy) {
            g2d.setColor(baseColor);
            g2d.fillOval(x + 3, y, enemySize - 6, enemySize); // mas estrecho
            g2d.setColor(baseColor.brighter());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x + 3, y, enemySize - 6, enemySize);
        } else {
            g2d.setColor(baseColor);
            g2d.fillOval(x, y, enemySize, enemySize);
            g2d.setColor(baseColor.brighter());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x, y, enemySize, enemySize);
            if (enemy instanceof PatrolEnemy) {
                g2d.drawOval(x - 2, y - 2, enemySize + 4, enemySize + 4);
            }
            if (enemy instanceof FastEnemy) {
                g2d.setColor(new Color(200, 200, 255, 150));
                g2d.drawLine(x - 4, y + 5, x - 1, y + 5);
                g2d.drawLine(x - 5, y + 10, x - 1, y + 10);
                g2d.drawLine(x - 4, y + 15, x - 1, y + 15);
            }
        }
        g2d.setStroke(new BasicStroke(1));
    }

    // -----------------------------------------------------------------------
    // Elementos especiales
    // -----------------------------------------------------------------------

    /**
     * Dibuja un elemento especial. Los elementos usados no se dibujan.
     */
    public static void drawSpecialElement(Graphics2D g2d, SpecialElement se, int offsetY) {
        if (se.isUsed()) return;

        int size = 18;
        int x = se.getPosition().getCol() * CELL_SIZE + (CELL_SIZE - size) / 2;
        int y = se.getPosition().getRow() * CELL_SIZE + (CELL_SIZE - size) / 2 + offsetY;

        if (se instanceof LifeSource) {
            // Cruz verde brillante
            g2d.setColor(new Color(80, 220, 80));
            g2d.fillRoundRect(x, y, size, size, 6, 6);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            int cx = x + size / 2;
            int cy = y + size / 2;
            int arm = 4;
            g2d.drawLine(cx, cy - arm, cx, cy + arm); // vertical
            g2d.drawLine(cx - arm, cy, cx + arm, cy); // horizontal
            g2d.setStroke(new BasicStroke(1));

        } else if (se instanceof Bomb) {
            // Circulo rojo oscuro con una X
            g2d.setColor(new Color(180, 30, 30));
            g2d.fillOval(x, y, size, size);
            g2d.setColor(new Color(255, 100, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x, y, size, size);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            int xw = g2d.getFontMetrics().stringWidth("!");
            g2d.drawString("!", x + (size - xw) / 2, y + (size + 8) / 2);
            g2d.setStroke(new BasicStroke(1));
        }
    }

    // -----------------------------------------------------------------------
    // Mensaje centrado
    // -----------------------------------------------------------------------

    public static void drawCenteredMessage(Graphics2D g2d, String message, String subtitle,
            int width, int height) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        int msgWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (width - msgWidth) / 2, height / 2 - 15);

        if (subtitle != null) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
            g2d.drawString(subtitle, (width - subWidth) / 2, height / 2 + 20);
        }
    }

    // -----------------------------------------------------------------------
    // Utilidades
    // -----------------------------------------------------------------------

    private static Color getCellColor(Cell cell) {
        switch (cell) {
            case WALL:              return COLOR_WALL;
            case START:             return COLOR_START;
            case SAFE_INTERMEDIATE: return COLOR_SAFE_INTER;
            case SAFE_FINAL:        return COLOR_SAFE_FINAL;
            case SAFE_FINAL_2:      return COLOR_SAFE_FINAL2;
            default:                return COLOR_EMPTY;
        }
    }

    private static Color getCoinColor(CoinType type) {
        switch (type) {
            case YELLOW:    return COLOR_COIN_YELLOW;
            case RED_SKIN:  return COLOR_COIN_RED;
            case BLUE_SKIN: return COLOR_COIN_BLUE;
            case GREEN_SKIN:return COLOR_COIN_GREEN;
            default:        return COLOR_COIN_YELLOW;
        }
    }
}
