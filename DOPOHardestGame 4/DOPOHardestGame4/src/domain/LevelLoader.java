package domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Carga niveles del juego desde archivos .txt
 */
public class LevelLoader {

    public static LevelConfig loadLevel(String filePath, String levelName) throws GameException {
        int timeLimit = 60; // default
        Cell[][] grid = new Cell[15][20];
        Position start = null;
        Position start2 = null;

        List<LevelConfig.CoinConfig> coins = new ArrayList<>();
        List<LevelConfig.EnemyConfig> enemies = new ArrayList<>();
        List<LevelConfig.SpecialElementConfig> specials = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int row = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("TIME:")) {
                    try {
                        timeLimit = Integer.parseInt(line.substring(5).trim());
                    } catch (NumberFormatException e) {
                        GameLogger.warning("Formato de tiempo invalido en " + filePath);
                    }
                    continue;
                }

                if (row < 15 && line.length() >= 20) {
                    for (int col = 0; col < 20; col++) {
                        char c = line.charAt(col);
                        Position pos = new Position(row, col);

                        switch (c) {
                            case '0':
                                grid[row][col] = Cell.EMPTY;
                                break;
                            case '1':
                                grid[row][col] = Cell.WALL;
                                break;
                            case 'S':
                                grid[row][col] = Cell.START;
                                if (start == null) start = pos;
                                break;
                            case 'I':
                                grid[row][col] = Cell.SAFE_INTERMEDIATE;
                                break;
                            case 'F':
                                grid[row][col] = Cell.SAFE_FINAL;
                                if (start2 == null) start2 = pos;
                                break;
                            case 'C':
                                grid[row][col] = Cell.EMPTY;
                                coins.add(new LevelConfig.CoinConfig(CoinType.YELLOW, pos));
                                break;
                            case 'B':
                                grid[row][col] = Cell.EMPTY;
                                coins.add(new LevelConfig.CoinConfig(CoinType.BLUE_SKIN, pos));
                                break;
                            case 'R':
                                grid[row][col] = Cell.EMPTY;
                                coins.add(new LevelConfig.CoinConfig(CoinType.RED_SKIN, pos));
                                break;
                            case 'G':
                                grid[row][col] = Cell.EMPTY;
                                coins.add(new LevelConfig.CoinConfig(CoinType.GREEN_SKIN, pos));
                                break;
                            case 'H':
                                grid[row][col] = Cell.EMPTY;
                                // Basic horizontal (RIGHT)
                                enemies.add(new LevelConfig.EnemyConfig(LevelConfig.EnemyType.BASIC, pos, Direction.RIGHT));
                                break;
                            case 'V':
                                grid[row][col] = Cell.EMPTY;
                                // Vertical (DOWN)
                                enemies.add(new LevelConfig.EnemyConfig(LevelConfig.EnemyType.VERTICAL, pos, Direction.DOWN));
                                break;
                            case 'Q':
                                grid[row][col] = Cell.EMPTY;
                                // Fast horizontal (RIGHT)
                                enemies.add(new LevelConfig.EnemyConfig(LevelConfig.EnemyType.FAST, pos, Direction.RIGHT));
                                break;
                            case 'P':
                                grid[row][col] = Cell.EMPTY;
                                // Patrol 4x5 default box
                                int rMax = Math.min(14, row + 4);
                                int cMax = Math.min(19, col + 5);
                                List<Position> waypoints = Arrays.asList(
                                    new Position(row, cMax),
                                    new Position(rMax, cMax),
                                    new Position(rMax, col),
                                    new Position(row, col)
                                );
                                enemies.add(new LevelConfig.EnemyConfig(pos, waypoints));
                                break;
                            case '+':
                                grid[row][col] = Cell.EMPTY;
                                specials.add(new LevelConfig.SpecialElementConfig(LevelConfig.SpecialElementType.LIFE_SOURCE, pos));
                                break;
                            case 'X':
                                grid[row][col] = Cell.EMPTY;
                                specials.add(new LevelConfig.SpecialElementConfig(LevelConfig.SpecialElementType.BOMB, pos));
                                break;
                            default:
                                grid[row][col] = Cell.EMPTY;
                                break;
                        }
                    }
                    row++;
                }
            }
        } catch (IOException e) {
            throw new GameException("Error leyendo nivel " + filePath, e);
        }

        if (start == null) {
            start = new Position(1, 1); // fallback
        }
        if (start2 == null) {
            start2 = new Position(1, 18); // fallback
        }

        return new LevelConfig(levelName, grid, start, start2, timeLimit, coins, enemies, specials);
    }
}
