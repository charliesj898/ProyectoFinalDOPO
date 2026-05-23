package persistence;

import domain.AIProfile;
import domain.Cell;
import domain.Coin;
import domain.Game;
import domain.GameMode;
import domain.GameState;
import domain.LevelConfig;
import domain.LevelFactory;
import domain.Player;
import domain.PlayerType;
import domain.Position;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion de GamePersistence en formato de texto plano legible.
 * Permite a los usuarios inspeccionar y modificar sus partidas guardadas usando el Bloc de notas.
 */
public class TextGamePersistence implements GamePersistence {

    @Override
    public void save(Game game, String filename) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("# DOPO Hardest Game Save State (Plain Text Format)\n");
            writer.write("# Este archivo es legible y editable. Modifica los valores con cuidado en el Bloc de notas.\n\n");

            // --- SECCIÓN: Game ---
            writer.write("[Game]\n");
            writer.write("mode=" + game.getMode().name() + "\n");
            writer.write("currentLevelIndex=" + game.getCurrentLevelIndex() + "\n");
            writer.write("state=" + game.getState().name() + "\n");
            writer.write("aiProfile=" + (game.getAIProfile() != null ? game.getAIProfile().name() : "null") + "\n\n");

            // --- SECCIÓN: Timer ---
            writer.write("[Timer]\n");
            writer.write("timeLimitSeconds=" + (int)(game.getTimer().getElapsedMillis() / 1000 + game.getTimer().getRemainingSeconds()) + "\n");
            writer.write("pausedElapsedMillis=" + game.getTimer().getElapsedMillis() + "\n");
            writer.write("running=" + game.getTimer().isRunning() + "\n");
            writer.write("paused=" + game.getTimer().isPaused() + "\n\n");

            // --- SECCIÓN: Players ---
            List<Player> players = game.getPlayers();
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                writer.write("[Player" + (i + 1) + "]\n");
                writer.write("name=" + p.getName() + "\n");
                writer.write("originalType=" + p.getOriginalType().name() + "\n");
                writer.write("activeType=" + p.getType().name() + "\n");
                writer.write("posX=" + p.getPosition().getRow() + "\n");
                writer.write("posY=" + p.getPosition().getCol() + "\n");
                writer.write("respawnX=" + p.getRespawnPoint().getRow() + "\n");
                writer.write("respawnY=" + p.getRespawnPoint().getCol() + "\n");
                writer.write("deaths=" + p.getDeaths() + "\n");
                writer.write("speed=" + p.getSpeed() + "\n");
                writer.write("shieldActive=" + p.isShieldActive() + "\n");
                writer.write("coinsCollected=" + p.getCoinsCollected() + "\n");
                writer.write("invincible=" + p.isInvincible() + "\n");
                writer.write("invincibilityTicks=0\n"); // safe default
                writer.write("reachedGoal=" + p.hasReachedGoal() + "\n");
                writer.write("levelGoalReachedTimeMs=" + p.getLevelGoalReachedTimeMs() + "\n");
                writer.write("totalGoalReachedTimeMs=" + p.getTotalGoalReachedTimeMs() + "\n");
                writer.write("levelWins=" + p.getLevelWins() + "\n");
                String hexColor = String.format("#%06x", p.getBorderColor().getRGB() & 0x00FFFFFF);
                writer.write("borderColor=" + hexColor + "\n");
                writer.write("goalCell=" + p.getGoalCell().name() + "\n\n");
            }

            // --- SECCIÓN: Coins ---
            writer.write("[Coins]\n");
            StringBuilder coinsSb = new StringBuilder();
            List<Coin> levelCoins = game.getCurrentLevel().getCoins();
            for (int i = 0; i < levelCoins.size(); i++) {
                coinsSb.append(levelCoins.get(i).isCollected());
                if (i < levelCoins.size() - 1) {
                    coinsSb.append(",");
                }
            }
            writer.write("collected=" + coinsSb.toString() + "\n");
        }
    }

    @Override
    public Game load(String filename) throws Exception {
        Map<String, Map<String, String>> sections = parseIni(filename);

        // 1. Obtener datos de Game
        Map<String, String> gameMap = sections.get("Game");
        if (gameMap == null) {
            throw new IllegalArgumentException("Seccion [Game] no encontrada en el archivo de guardado.");
        }
        GameMode mode = GameMode.valueOf(gameMap.get("mode"));
        int currentLevelIndex = Integer.parseInt(gameMap.get("currentLevelIndex"));
        GameState state = GameState.valueOf(gameMap.get("state"));
        AIProfile aiProfile = null;
        if (gameMap.containsKey("aiProfile") && !gameMap.get("aiProfile").equals("null")) {
            aiProfile = AIProfile.valueOf(gameMap.get("aiProfile"));
        }

        // 2. Reconstruir lista de Jugadores
        List<Player> players = new ArrayList<>();
        Map<String, String> p1Map = sections.get("Player1");
        if (p1Map == null) {
            throw new IllegalArgumentException("Seccion [Player1] no encontrada.");
        }
        Player p1 = createPlayerFromMap(p1Map);
        players.add(p1);

        Map<String, String> p2Map = sections.get("Player2");
        if (p2Map != null) {
            Player p2 = createPlayerFromMap(p2Map);
            players.add(p2);
        }

        // 3. Cargar configuraciones de nivel estaticas
        List<LevelConfig> levelConfigs = LevelFactory.createAllLevels();

        // 4. Instanciar la partida Game
        Game game = new Game(mode, players, levelConfigs, aiProfile);

        // 5. Cargar el nivel especifico (esto instancia el Level y sus monedas/enemigos por defecto)
        game.loadLevelForRestore(currentLevelIndex);

        // 6. Restaurar estados individuales de cada jugador (ahora que estan vinculados al juego)
        restorePlayerFromMap(game.getPlayers().get(0), p1Map);
        if (players.size() > 1 && p2Map != null) {
            restorePlayerFromMap(game.getPlayers().get(1), p2Map);
        }

        // 7. Restaurar temporizador
        Map<String, String> timerMap = sections.get("Timer");
        if (timerMap != null) {
            int timeLimit = Integer.parseInt(timerMap.get("timeLimitSeconds"));
            long pausedElapsed = Long.parseLong(timerMap.get("pausedElapsedMillis"));
            boolean running = Boolean.parseBoolean(timerMap.get("running"));
            boolean paused = Boolean.parseBoolean(timerMap.get("paused"));
            game.getTimer().restoreState(timeLimit, pausedElapsed, running, paused);
        }

        // 8. Restaurar monedas recolectadas en el nivel actual
        Map<String, String> coinsMap = sections.get("Coins");
        if (coinsMap != null && coinsMap.containsKey("collected")) {
            String collectedStr = coinsMap.get("collected").trim();
            if (!collectedStr.isEmpty()) {
                String[] tokens = collectedStr.split(",");
                List<Coin> levelCoins = game.getCurrentLevel().getCoins();
                for (int i = 0; i < levelCoins.size() && i < tokens.length; i++) {
                    boolean isColl = Boolean.parseBoolean(tokens[i].trim());
                    levelCoins.get(i).setCollected(isColl);
                }
            }
        }

        // 9. Restaurar estado del juego
        game.setState(state);

        return game;
    }

    private Player createPlayerFromMap(Map<String, String> pMap) {
        String name = pMap.get("name");
        PlayerType originalType = PlayerType.valueOf(pMap.get("originalType"));
        int posX = Integer.parseInt(pMap.get("posX"));
        int posY = Integer.parseInt(pMap.get("posY"));
        return new Player(name, originalType, new Position(posX, posY));
    }

    private void restorePlayerFromMap(Player p, Map<String, String> pMap) {
        int posX = Integer.parseInt(pMap.get("posX"));
        int posY = Integer.parseInt(pMap.get("posY"));
        int respawnX = Integer.parseInt(pMap.get("respawnX"));
        int respawnY = Integer.parseInt(pMap.get("respawnY"));
        int deaths = Integer.parseInt(pMap.get("deaths"));
        float speed = Float.parseFloat(pMap.get("speed"));
        boolean shieldActive = Boolean.parseBoolean(pMap.get("shieldActive"));
        int coinsCollected = 0;
        if (pMap.containsKey("coinsCollected")) {
            coinsCollected = Integer.parseInt(pMap.get("coinsCollected"));
        }
        boolean invincible = Boolean.parseBoolean(pMap.get("invincible"));
        int invincibilityTicks = Integer.parseInt(pMap.get("invincibilityTicks"));
        boolean reachedGoal = Boolean.parseBoolean(pMap.get("reachedGoal"));
        long levelGoalReachedTimeMs = Long.parseLong(pMap.get("levelGoalReachedTimeMs"));
        long totalGoalReachedTimeMs = Long.parseLong(pMap.get("totalGoalReachedTimeMs"));
        int levelWins = Integer.parseInt(pMap.get("levelWins"));
        Color borderColor = Color.decode(pMap.get("borderColor"));
        Cell goalCell = Cell.valueOf(pMap.get("goalCell"));
        PlayerType activeType = PlayerType.valueOf(pMap.get("activeType"));

        p.restoreState(
            new Position(posX, posY),
            new Position(respawnX, respawnY),
            deaths,
            speed,
            shieldActive,
            coinsCollected,
            invincible,
            invincibilityTicks,
            reachedGoal,
            levelGoalReachedTimeMs,
            totalGoalReachedTimeMs,
            levelWins,
            borderColor,
            goalCell,
            activeType
        );
    }

    private Map<String, Map<String, String>> parseIni(String filepath) throws Exception {
        Map<String, Map<String, String>> sections = new HashMap<>();
        Map<String, String> currentSection = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    String sectionName = line.substring(1, line.length() - 1).trim();
                    currentSection = new HashMap<>();
                    sections.put(sectionName, currentSection);
                } else if (currentSection != null && line.contains("=")) {
                    int eqIndex = line.indexOf('=');
                    String key = line.substring(0, eqIndex).trim();
                    String val = line.substring(eqIndex + 1).trim();
                    currentSection.put(key, val);
                }
            }
        }
        return sections;
    }
}
