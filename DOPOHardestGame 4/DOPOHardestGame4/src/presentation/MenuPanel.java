package presentation;

import domain.GameMode;
import persistence.GamePersistence;
import persistence.TextGamePersistence;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Pantalla de menu principal.
 * Muestra el titulo del juego y opciones para seleccionar modo de juego,
 * o cargar una partida guardada.
 */
public class MenuPanel extends JPanel {

    private static final Color BG_COLOR = new Color(30, 30, 40);
    private static final Color TITLE_COLOR = new Color(220, 50, 50);
    private static final Color SUBTITLE_COLOR = new Color(200, 200, 200);

    private final MainWindow mainWindow;

    public MenuPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setBackground(BG_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        buildUI();
    }

    private void buildUI() {
        // Espaciador superior
        add(Box.createVerticalGlue());

        // Titulo
        JLabel titleLabel = createLabel("THE DOPO HARDEST GAME", TITLE_COLOR, 28, Font.BOLD);
        add(titleLabel);

        add(Box.createRigidArea(new Dimension(0, 10)));

        // Subtitulo
        JLabel subtitleLabel = createLabel("Puedes superar los 3 niveles?", SUBTITLE_COLOR, 14, Font.PLAIN);
        add(subtitleLabel);

        add(Box.createRigidArea(new Dimension(0, 50)));

        // Botones de modos de juego
        JButton singlePlayerBtn = createMenuButton("Un Jugador");
        singlePlayerBtn.addActionListener(e -> {
            mainWindow.showConfig(GameMode.SINGLE_PLAYER);
        });
        add(singlePlayerBtn);

        add(Box.createRigidArea(new Dimension(0, 15)));

        JButton pvpBtn = createMenuButton("Jugador vs Jugador");
        pvpBtn.addActionListener(e -> {
            mainWindow.showConfig(GameMode.PLAYER_VS_PLAYER);
        });
        add(pvpBtn);

        add(Box.createRigidArea(new Dimension(0, 15)));

        JButton pvmBtn = createMenuButton("Jugador vs Maquina");
        pvmBtn.addActionListener(e -> {
            mainWindow.showConfig(GameMode.PLAYER_VS_MACHINE);
        });
        add(pvmBtn);

        add(Box.createRigidArea(new Dimension(0, 25)));

        // Boton de cargar partida
        JButton loadBtn = createMenuButton("Cargar Partida");
        loadBtn.addActionListener(e -> {
            java.io.File saveDir = new java.io.File("saves");
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(saveDir);
            fileChooser.setDialogTitle("Cargar Partida");
            javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Archivos de Partida (*.dat)", "dat");
            fileChooser.setFileFilter(filter);

            int userSelection = fileChooser.showOpenDialog(this);

            if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File fileToLoad = fileChooser.getSelectedFile();
                try {
                    GamePersistence persistence = new TextGamePersistence();
                    domain.Game loadedGame = persistence.load(fileToLoad.getAbsolutePath());
                    loadedGame.resume(); // Quitar la pausa automaticamente al cargar
                    mainWindow.resumeGame(loadedGame);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Error al cargar la partida: el archivo puede estar corrupto o no ser válido.", "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(loadBtn);

        // Espaciador inferior
        add(Box.createVerticalGlue());
    }

    private JLabel createLabel(String text, Color color, int fontSize, int fontStyle) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Arial", fontStyle, fontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(300, 45));
        button.setPreferredSize(new Dimension(300, 45));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(60, 60, 80));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 130), 2));
        button.setOpaque(true);

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(80, 80, 110));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(60, 60, 80));
                }
            }
        });

        return button;
    }
}
