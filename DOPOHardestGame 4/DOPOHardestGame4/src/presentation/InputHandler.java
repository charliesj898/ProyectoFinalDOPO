package presentation;

import domain.Direction;
import domain.Game;
import domain.GameMode;
import persistence.GamePersistence;
import persistence.TextGamePersistence;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Maneja la entrada de teclado para los jugadores.
 *
 * Controles P1 (WASD):
 * W / S / A / D = cardinal
 * W+D / W+A / S+D / S+A = diagonal
 *
 * Controles P2 (Flechas):
 * UP / DOWN / LEFT / RIGHT = cardinal
 * UP+RIGHT / UP+LEFT / DOWN+RIGHT ... = diagonal
 *
 * P = Pausar/Reanudar
 * ESC = Salir al menu
 */
public class InputHandler extends KeyAdapter {

    private final Game game;
    private final GamePanel gamePanel;
    private final Set<Integer> pressedKeys;

    public InputHandler(Game game, GamePanel gamePanel) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.pressedKeys = new HashSet<>();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        pressedKeys.add(key);

        switch (key) {
            case KeyEvent.VK_P:
                gamePanel.togglePause();
                return;
            case KeyEvent.VK_G:
                if (game.getState() == domain.GameState.PAUSED) {
                    java.io.File saveDir = new java.io.File("saves");
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    }
                    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(saveDir);
                    fileChooser.setDialogTitle("Guardar Partida");
                    javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter(
                            "Archivos de Partida (*.dat)", "dat");
                    fileChooser.setFileFilter(filter);
                    // Generar un nombre de archivo descriptivo y único por defecto
                    StringBuilder defaultName = new StringBuilder("partida_");
                    if (game.getPlayers() != null && !game.getPlayers().isEmpty()) {
                        String p1Name = game.getPlayers().get(0).getName().replaceAll("[\\\\/:*?\"<>|\\s]", "_");
                        defaultName.append(p1Name);
                        if (game.getPlayers().size() > 1) {
                            String p2Name = game.getPlayers().get(1).getName().replaceAll("[\\\\/:*?\"<>|\\s]", "_");
                            defaultName.append("_vs_").append(p2Name);
                        }
                    }
                    defaultName.append("_Nivel_").append(game.getCurrentLevelIndex() + 1);

                    // Agregar marca de tiempo para evitar duplicados
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
                    String timestamp = sdf.format(new java.util.Date());
                    defaultName.append("_").append(timestamp).append(".dat");

                    fileChooser.setSelectedFile(new java.io.File(saveDir, defaultName.toString()));

                    int userSelection = fileChooser.showSaveDialog(gamePanel);

                    if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                        java.io.File fileToSave = fileChooser.getSelectedFile();
                        if (!fileToSave.getName().toLowerCase().endsWith(".dat")) {
                            fileToSave = new java.io.File(fileToSave.getParentFile(), fileToSave.getName() + ".dat");
                        }
                        try {
                            GamePersistence persistence = new TextGamePersistence();
                            persistence.save(game, fileToSave.getAbsolutePath());
                            javax.swing.JOptionPane.showMessageDialog(gamePanel,
                                    "Partida guardada correctamente en:\n" + fileToSave.getName(), "Guardar",
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            javax.swing.JOptionPane.showMessageDialog(gamePanel, "Error al guardar la partida.",
                                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                return;
            case KeyEvent.VK_ESCAPE:
                gamePanel.exitToMenu();
                return;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    /**
     * Procesa las teclas presionadas y mueve a los jugadores.
     * Llamado en cada tick del game loop.
     */
    public void processInput() {
        // --- Jugador 1 (WASD) ---
        Direction dir1 = resolveDirection(
                pressedKeys.contains(KeyEvent.VK_W),
                pressedKeys.contains(KeyEvent.VK_S),
                pressedKeys.contains(KeyEvent.VK_A),
                pressedKeys.contains(KeyEvent.VK_D));
        if (dir1 != null) {
            game.movePlayer(0, dir1);
        }

        // --- Jugador 2 (Flechas, solo PvsP) ---
        if (game.getMode() == GameMode.PLAYER_VS_PLAYER) {
            Direction dir2 = resolveDirection(
                    pressedKeys.contains(KeyEvent.VK_UP),
                    pressedKeys.contains(KeyEvent.VK_DOWN),
                    pressedKeys.contains(KeyEvent.VK_LEFT),
                    pressedKeys.contains(KeyEvent.VK_RIGHT));
            if (dir2 != null) {
                game.movePlayer(1, dir2);
            }
        }
    }

    /**
     * Convierte el estado de cuatro teclas cardinales en una Direction,
     * incluyendo combinaciones diagonales.
     *
     * @param up    tecla arriba presionada
     * @param down  tecla abajo presionada
     * @param left  tecla izquierda presionada
     * @param right tecla derecha presionada
     * @return la direccion resultante, o null si no hay tecla activa
     */
    private Direction resolveDirection(boolean up, boolean down, boolean left, boolean right) {
        // Diagonales (combinaciones de dos teclas)
        if (up && right)
            return Direction.UP_RIGHT;
        if (up && left)
            return Direction.UP_LEFT;
        if (down && right)
            return Direction.DOWN_RIGHT;
        if (down && left)
            return Direction.DOWN_LEFT;

        // Cardinales
        if (up)
            return Direction.UP;
        if (down)
            return Direction.DOWN;
        if (left)
            return Direction.LEFT;
        if (right)
            return Direction.RIGHT;

        return null;
    }
}
