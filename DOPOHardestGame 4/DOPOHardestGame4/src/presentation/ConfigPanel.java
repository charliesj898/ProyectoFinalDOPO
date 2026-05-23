package presentation;

import domain.AIProfile;
import domain.Game;
import domain.GameMode;
import domain.LevelConfig;
import domain.LevelFactory;
import domain.Player;
import domain.PlayerType;
import domain.Position;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla de configuracion de partida.
 * Permite seleccionar modo de juego, nombre, tipo y color de borde de cada jugador.
 * En modo PvsM agrega el selector de perfil de IA.
 */
public class ConfigPanel extends JPanel {

    private static final Color BG_COLOR   = new Color(30, 30, 40);
    private static final Color TEXT_COLOR = new Color(200, 200, 200);

    // Colores de borde predefinidos para que el jugador elija
    private static final Color[] BORDER_COLORS = {
        Color.WHITE, Color.YELLOW, new Color(255, 150, 50), Color.CYAN,
        Color.MAGENTA, new Color(180, 255, 180), Color.RED, Color.BLACK
    };
    private static final String[] BORDER_COLOR_NAMES = {
        "Blanco", "Amarillo", "Naranja", "Cian",
        "Magenta", "Verde claro", "Rojo", "Negro"
    };

    private final MainWindow mainWindow;

    private GameMode selectedMode;
    private JLabel   modeLabel;

    // P1
    private JTextField            player1NameField;
    private JComboBox<PlayerType> player1TypeCombo;
    private JComboBox<String>     player1BorderCombo;

    // P2
    private JTextField            player2NameField;
    private JComboBox<PlayerType> player2TypeCombo;
    private JComboBox<String>     player2BorderCombo;
    private JComboBox<AIProfile>  aiProfileCombo;

    // Panel raiz del formulario (para show/hide)
    private JPanel formPanel;

    // Referencias a labels de P2 para show/hide
    private JLabel p2NameLabel;
    private JLabel p2TypeLabel;
    private JLabel p2BorderLabel;
    private JLabel aiProfileLabel;

    public ConfigPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setBackground(BG_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        buildUI();
    }

    private void buildUI() {
        add(Box.createVerticalGlue());

        JLabel titleLabel = new JLabel("Configuracion de Partida");
        titleLabel.setForeground(new Color(220, 50, 50));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);

        add(Box.createRigidArea(new Dimension(0, 20)));

        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        formPanel.setMaximumSize(new Dimension(500, 400));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // --- Modo (solo lectura) ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Modo de juego:"), gbc);
        gbc.gridx = 1;
        selectedMode = GameMode.SINGLE_PLAYER;
        modeLabel = new JLabel(selectedMode.getDisplayName());
        modeLabel.setForeground(new Color(180, 180, 255));
        modeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        modeLabel.setPreferredSize(new Dimension(200, 30));
        formPanel.add(modeLabel, gbc);
        row++;

        // --- P1: nombre ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Nombre Jugador 1:"), gbc);
        gbc.gridx = 1;
        player1NameField = createTextField("Jugador 1");
        formPanel.add(player1NameField, gbc);
        row++;

        // --- P1: tipo ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Tipo Jugador 1:"), gbc);
        gbc.gridx = 1;
        player1TypeCombo = new JComboBox<>(PlayerType.values());
        styleComboBox(player1TypeCombo);
        formPanel.add(player1TypeCombo, gbc);
        row++;

        // --- P1: color borde ---
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(createLabel("Borde Jugador 1:"), gbc);
        gbc.gridx = 1;
        player1BorderCombo = new JComboBox<>(BORDER_COLOR_NAMES);
        styleComboBox(player1BorderCombo);
        formPanel.add(player1BorderCombo, gbc);
        row++;

        // --- P2: nombre ---
        gbc.gridx = 0; gbc.gridy = row;
        p2NameLabel = createLabel("Nombre Jugador 2:");
        formPanel.add(p2NameLabel, gbc);
        gbc.gridx = 1;
        player2NameField = createTextField("Jugador 2");
        formPanel.add(player2NameField, gbc);
        row++;

        // --- P2: tipo ---
        gbc.gridx = 0; gbc.gridy = row;
        p2TypeLabel = createLabel("Tipo Jugador 2:");
        formPanel.add(p2TypeLabel, gbc);
        gbc.gridx = 1;
        player2TypeCombo = new JComboBox<>(PlayerType.values());
        player2TypeCombo.setSelectedItem(PlayerType.BLUE);
        styleComboBox(player2TypeCombo);
        formPanel.add(player2TypeCombo, gbc);
        row++;

        // --- P2: color borde ---
        gbc.gridx = 0; gbc.gridy = row;
        p2BorderLabel = createLabel("Borde Jugador 2:");
        formPanel.add(p2BorderLabel, gbc);
        gbc.gridx = 1;
        player2BorderCombo = new JComboBox<>(BORDER_COLOR_NAMES);
        player2BorderCombo.setSelectedIndex(1); // Amarillo por defecto
        styleComboBox(player2BorderCombo);
        formPanel.add(player2BorderCombo, gbc);
        row++;

        // --- Perfil de IA (solo PvsM) ---
        gbc.gridx = 0; gbc.gridy = row;
        aiProfileLabel = createLabel("Perfil IA:");
        formPanel.add(aiProfileLabel, gbc);
        gbc.gridx = 1;
        aiProfileCombo = new JComboBox<>(AIProfile.values());
        styleComboBox(aiProfileCombo);
        formPanel.add(aiProfileCombo, gbc);

        add(formPanel);
        add(Box.createRigidArea(new Dimension(0, 20)));

        // Botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(400, 50));

        JButton backBtn = createButton("Volver", new Color(100, 100, 120));
        backBtn.addActionListener(e -> mainWindow.showMenu());
        buttonPanel.add(backBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));

        JButton startBtn = createButton("Iniciar Juego", new Color(50, 150, 50));
        startBtn.addActionListener(e -> startGame());
        buttonPanel.add(startBtn);

        add(buttonPanel);
        add(Box.createVerticalGlue());

        updatePlayer2Visibility();
    }

    /**
     * Establece el modo de juego desde el menu principal (solo lectura).
     */
    public void setMode(GameMode mode) {
        this.selectedMode = mode;
        modeLabel.setText(mode.getDisplayName());
        updatePlayer2Visibility();
    }

    private void updatePlayer2Visibility() {
        boolean show2 = (selectedMode == GameMode.PLAYER_VS_PLAYER
                || selectedMode == GameMode.PLAYER_VS_MACHINE);
        boolean isPvM = (selectedMode == GameMode.PLAYER_VS_MACHINE);

        p2NameLabel.setVisible(show2);
        player2NameField.setVisible(show2);
        p2TypeLabel.setVisible(show2);
        player2TypeCombo.setVisible(show2);
        p2BorderLabel.setVisible(show2);
        player2BorderCombo.setVisible(show2);
        aiProfileLabel.setVisible(isPvM);
        aiProfileCombo.setVisible(isPvM);

        if (isPvM) {
            player2NameField.setText("Maquina");
            player2NameField.setEditable(false);
        } else {
            if ("Maquina".equals(player2NameField.getText())) {
                player2NameField.setText("Jugador 2");
            }
            player2NameField.setEditable(true);
        }

        revalidate();
        repaint();
    }

    // -----------------------------------------------------------------------
    // Inicio del juego
    // -----------------------------------------------------------------------

    private void startGame() {
        GameMode mode = selectedMode;

        String name1 = player1NameField.getText().trim();
        if (name1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre para el Jugador 1",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PlayerType type1   = (PlayerType) player1TypeCombo.getSelectedItem();
        Color borderColor1 = BORDER_COLORS[player1BorderCombo.getSelectedIndex()];

        List<Player> players = new ArrayList<>();
        Player p1 = new Player(name1, type1, new Position(0, 0));
        p1.setBorderColor(borderColor1);
        players.add(p1);

        AIProfile aiProfile = null;

        if (mode == GameMode.PLAYER_VS_PLAYER || mode == GameMode.PLAYER_VS_MACHINE) {
            String name2 = player2NameField.getText().trim();
            if (name2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese un nombre para el Jugador 2",
                        "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            PlayerType type2   = (PlayerType) player2TypeCombo.getSelectedItem();
            Color borderColor2 = BORDER_COLORS[player2BorderCombo.getSelectedIndex()];

            Player p2 = new Player(name2, type2, new Position(0, 0));
            p2.setBorderColor(borderColor2);
            players.add(p2);

            if (mode == GameMode.PLAYER_VS_MACHINE) {
                aiProfile = (AIProfile) aiProfileCombo.getSelectedItem();
            }
        }

        List<LevelConfig> levels = LevelFactory.createAllLevels();
        Game game = new Game(mode, players, levels, aiProfile);
        mainWindow.startGame(game);
    }

    // -----------------------------------------------------------------------
    // Utilidades de UI
    // -----------------------------------------------------------------------

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setPreferredSize(new Dimension(200, 30));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        return field;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(200, 30));
        combo.setFont(new Font("Arial", Font.PLAIN, 13));
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(bgColor.brighter(), 2));
        button.setOpaque(true);
        return button;
    }
}
