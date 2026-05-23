package test;

import org.junit.Before;
import org.junit.Test;

import domain.*;
import domain.Board;
import domain.Cell;
import domain.CollisionDetector;
import domain.Direction;
import domain.Player;
import domain.PlayerType;
import domain.Position;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Pruebas unitarias para la logica de dominio del juego DOPO Hardest Game.
 * Cubre movimiento, colisiones, estados del jugador, escudos y reglas de
 * victoria.
 */
public class GameDomainTest {

    private Player player1;
    private Player player2;
    private Board board;
    private CollisionDetector collisionDetector;

    // Tablero 3x3 para pruebas basicas:
    // [PARED] [PARED] [PARED]
    // [INICIO][VACIO] [FINAL]
    // [PARED] [INTERMEDIO][PARED]
    @Before
    public void setUp() {
        player1 = new Player("Player 1", PlayerType.RED, new Position(1, 0));
        player2 = new Player("Player 2", PlayerType.BLUE, new Position(1, 17));

        Cell[][] grid = {
                { Cell.WALL, Cell.WALL, Cell.WALL },
                { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL },
                { Cell.WALL, Cell.SAFE_INTERMEDIATE, Cell.WALL }
        };
        board = new Board(grid);
        collisionDetector = new CollisionDetector();
    }

    // ==========================================
    // 1. PRUEBAS DE POSICION Y COORDENADAS
    // ==========================================

    @Test
    public void testPositionMove() {
        Position pos = new Position(1, 1);
        assertEquals("Arriba", new Position(0, 1), pos.move(Direction.UP));
        assertEquals("Abajo", new Position(2, 1), pos.move(Direction.DOWN));
        assertEquals("Izquierda", new Position(1, 0), pos.move(Direction.LEFT));
        assertEquals("Derecha", new Position(1, 2), pos.move(Direction.RIGHT));
    }

    @Test
    public void testPositionMoveDiagonal() {
        Position pos = new Position(2, 2);
        assertEquals("ArrDer", new Position(1, 3), pos.move(Direction.UP_RIGHT));
        assertEquals("ArrIzq", new Position(1, 1), pos.move(Direction.UP_LEFT));
        assertEquals("AbajoDer", new Position(3, 3), pos.move(Direction.DOWN_RIGHT));
        assertEquals("AbajoIzq", new Position(3, 1), pos.move(Direction.DOWN_LEFT));
    }

    @Test
    public void testPositionDistance() {
        Position p1 = new Position(0, 0);
        Position p2 = new Position(3, 4);
        assertEquals("Distancia Manhattan", 7, p1.distanceTo(p2));
    }

    @Test
    public void testPositionEquality() {
        Position a = new Position(2, 3);
        Position b = new Position(2, 3);
        Position c = new Position(1, 3);
        assertEquals("Iguales", a, b);
        assertNotEquals("Distintos", a, c);
        assertNotEquals("Vs null", a, null);
        assertNotEquals("Vs otro tipo", a, "cadena");
        assertEquals("hashCode igual", a.hashCode(), b.hashCode());
    }

    @Test
    public void testPositionSelf() {
        Position a = new Position(5, 5);
        assertEquals("Igual a si mismo", a, a);
    }

    @Test
    public void testPositionToString() {
        Position pos = new Position(3, 7);
        assertEquals("(3, 7)", pos.toString());
    }

    @Test
    public void testPositionGetters() {
        Position pos = new Position(4, 6);
        assertEquals(4, pos.getRow());
        assertEquals(6, pos.getCol());
    }

    // ==========================================
    // 2. PRUEBAS DEL TABLERO
    // ==========================================

    @Test
    public void testBoardWalkable() {
        assertTrue("Celda vacia es transitable", board.isWalkable(new Position(1, 1)));
        assertTrue("Celda de inicio es transitable", board.isWalkable(new Position(1, 0)));
        assertFalse("Pared no es transitable", board.isWalkable(new Position(0, 0)));
        assertFalse("Fuera de limites negativo", board.isWalkable(new Position(-1, 0)));
        assertFalse("Fuera de limites positivo", board.isWalkable(new Position(3, 3)));
    }

    @Test
    public void testBoardGetCell() {
        assertEquals(Cell.WALL, board.getCell(0, 0));
        assertEquals(Cell.EMPTY, board.getCell(1, 1));
        assertEquals(Cell.WALL, board.getCell(-1, 0)); // fuera de limites -> WALL
        assertEquals(Cell.SAFE_FINAL, board.getCell(new Position(1, 2)));
    }

    @Test
    public void testBoardDimensions() {
        assertEquals(3, board.getRows());
        assertEquals(3, board.getCols());
    }

    @Test
    public void testBoardIsInBounds() {
        assertTrue(board.isInBounds(0, 0));
        assertTrue(board.isInBounds(2, 2));
        assertFalse(board.isInBounds(-1, 0));
        assertFalse(board.isInBounds(3, 0));
        assertFalse(board.isInBounds(0, 3));
    }

    @Test
    public void testBoardSetCell() {
        Board b = new Board(3, 3);
        assertEquals(Cell.EMPTY, b.getCell(1, 1));
        b.setCell(1, 1, Cell.WALL);
        assertEquals(Cell.WALL, b.getCell(1, 1));
        // fuera de limites: no lanza excepcion
        b.setCell(-1, 0, Cell.WALL);
    }

    @Test
    public void testBoardEmptyConstructor() {
        Board b = new Board(2, 4);
        assertEquals(2, b.getRows());
        assertEquals(4, b.getCols());
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 4; c++) {
                assertEquals(Cell.EMPTY, b.getCell(r, c));
            }
        }
    }

    @Test
    public void testBoardGetGrid() {
        assertNotNull(board.getGrid());
        assertEquals(3, board.getGrid().length);
    }

    // ==========================================
    // 3. PRUEBAS DE DIRECTION
    // ==========================================

    @Test
    public void testDirectionOpposite() {
        assertEquals(Direction.DOWN, Direction.UP.opposite());
        assertEquals(Direction.UP, Direction.DOWN.opposite());
        assertEquals(Direction.RIGHT, Direction.LEFT.opposite());
        assertEquals(Direction.LEFT, Direction.RIGHT.opposite());
        assertEquals(Direction.DOWN_LEFT, Direction.UP_RIGHT.opposite());
        assertEquals(Direction.DOWN_RIGHT, Direction.UP_LEFT.opposite());
        assertEquals(Direction.UP_LEFT, Direction.DOWN_RIGHT.opposite());
        assertEquals(Direction.UP_RIGHT, Direction.DOWN_LEFT.opposite());
    }

    @Test
    public void testDirectionCardinals() {
        Direction[] cards = Direction.cardinals();
        assertEquals(4, cards.length);
        List<Direction> list = Arrays.asList(cards);
        assertTrue(list.contains(Direction.UP));
        assertTrue(list.contains(Direction.DOWN));
        assertTrue(list.contains(Direction.LEFT));
        assertTrue(list.contains(Direction.RIGHT));
    }

    @Test
    public void testDirectionDeltas() {
        assertEquals(-1, Direction.UP.getDeltaRow());
        assertEquals(0, Direction.UP.getDeltaCol());
        assertEquals(1, Direction.DOWN.getDeltaRow());
        assertEquals(0, Direction.DOWN.getDeltaCol());
        assertEquals(0, Direction.LEFT.getDeltaRow());
        assertEquals(-1, Direction.LEFT.getDeltaCol());
        assertEquals(0, Direction.RIGHT.getDeltaRow());
        assertEquals(1, Direction.RIGHT.getDeltaCol());
    }

    // ==========================================
    // 4. PRUEBAS DE JUGADOR (PLAYER)
    // ==========================================

    @Test
    public void testPlayerInitialization() {
        player1.resetForLevel(new Position(1, 0));
        assertEquals("Posicion inicial", new Position(1, 0), player1.getPosition());
        assertEquals("Muertes iniciales", 0, player1.getDeaths());
        assertEquals("Monedas iniciales", 0, player1.getCoinsCollected());
        assertFalse("Aun no llega a meta", player1.hasReachedGoal());
    }

    @Test
    public void testPlayerMovement() {
        player1.resetForLevel(new Position(1, 0));

        boolean moved = player1.tryMove(Direction.RIGHT, board);
        assertTrue("Se movio a espacio vacio", moved);
        assertEquals(new Position(1, 1), player1.getPosition());

        moved = player1.tryMove(Direction.UP, board);
        assertFalse("Choco con pared", moved);
        assertEquals(new Position(1, 1), player1.getPosition());
    }

    @Test
    public void testPlayerMovementCooldown() {
        player1.resetForLevel(new Position(1, 0));
        // Primer movimiento
        assertTrue(player1.tryMove(Direction.RIGHT, board));
        // Inmediatamente: cooldown activo, no puede moverse
        assertFalse("Cooldown activo", player1.tryMove(Direction.LEFT, board));
    }

    @Test
    public void testPlayerCanMoveAfterTicks() {
        player1.resetForLevel(new Position(1, 0));
        player1.tryMove(Direction.RIGHT, board);
        // Hacer suficientes ticks para salir del cooldown
        for (int i = 0; i < 10; i++) {
            player1.tick();
        }
        assertTrue("Puede moverse despues de ticks", player1.tryMove(Direction.LEFT, board));
    }

    @Test
    public void testPlayerGetters() {
        assertEquals("Player 1", player1.getName());
        assertEquals(PlayerType.RED, player1.getType());
        assertEquals(PlayerType.RED, player1.getOriginalType());
        assertNotNull(player1.getBorderColor());
        assertEquals(Cell.SAFE_FINAL, player1.getGoalCell());
        assertNotNull(player1.getPosition());
        assertNotNull(player1.getRespawnPoint());
        assertEquals(0, player1.getLevelWins());
        assertEquals(0, player1.getLevelGoalReachedTimeMs());
        assertEquals(0, player1.getTotalGoalReachedTimeMs());
    }

    @Test
    public void testPlayerToString() {
        assertNotNull(player1.toString());
        assertTrue(player1.toString().contains("Player 1"));
    }

    @Test
    public void testPlayerSetGoalCell() {
        player1.setGoalCell(Cell.START);
        assertEquals(Cell.START, player1.getGoalCell());
    }

    @Test
    public void testPlayerSetReachedGoal() {
        player1.setReachedGoal(true);
        assertTrue(player1.hasReachedGoal());
        player1.setReachedGoal(false);
        assertFalse(player1.hasReachedGoal());
    }

    @Test
    public void testPlayerCollectCoin() {
        player1.collectCoin();
        player1.collectCoin();
        assertEquals(2, player1.getCoinsCollected());
    }

    @Test
    public void testPlayerUpdateRespawnPoint() {
        player1.updateRespawnPoint(new Position(2, 2));
        assertEquals(new Position(2, 2), player1.getRespawnPoint());
    }

    @Test
    public void testPlayerSetLevelGoalTime() {
        player1.setLevelGoalReachedTimeMs(5000);
        assertEquals(5000, player1.getLevelGoalReachedTimeMs());
        player1.addLevelTime();
        assertEquals(5000, player1.getTotalGoalReachedTimeMs());
    }

    @Test
    public void testPlayerAddLevelWin() {
        player1.addLevelWin();
        player1.addLevelWin();
        assertEquals(2, player1.getLevelWins());
    }

    @Test
    public void testPlayerSetBorderColor() {
        java.awt.Color red = java.awt.Color.RED;
        player1.setBorderColor(red);
        assertEquals(red, player1.getBorderColor());
    }

    // ==========================================
    // 5. PRUEBAS DE SKINS Y ESCUDOS
    // ==========================================

    @Test
    public void testSkinEffect() {
        float originalSpeed = player1.getSpeed();

        player1.applySkinEffect(PlayerType.BLUE);
        assertTrue("Skin azul aumenta velocidad", player1.getSpeed() > originalSpeed);

        player1.applySkinEffect(PlayerType.GREEN);
        assertEquals("El tipo activo cambia a verde", PlayerType.GREEN, player1.getType());

        player1.resetSkin();
        assertEquals("Reset devuelve velocidad original", originalSpeed, player1.getSpeed(), 0.01f);
        assertFalse("Reset remueve escudo", player1.isShieldActive());
    }

    @Test
    public void testResetSkinWhenAlreadyOriginal() {
        // No deberia tirar excepcion si ya es el tipo original
        player1.resetSkin();
        assertEquals(PlayerType.RED, player1.getType());
    }

    @Test
    public void testShieldPreventsDeath() {
        player1.resetForLevel(new Position(1, 1));
        player1.activateShield();

        boolean died = player1.handleEnemyCollision();
        assertFalse("El escudo previene la muerte", died);
        assertFalse("El escudo se rompio", player1.isShieldActive());
        assertEquals("Sin muertes sumadas", 0, player1.getDeaths());

        for (int i = 0; i <= 30; i++) {
            player1.tick();
        }

        died = player1.handleEnemyCollision();
        assertTrue("Segundo golpe sin escudo mata", died);
        assertEquals("Muertes sumadas", 1, player1.getDeaths());
    }

    @Test
    public void testGreenPlayerHasShieldFromStart() {
        Player greenPlayer = new Player("Green", PlayerType.GREEN, new Position(1, 1));
        assertTrue("GREEN tiene escudo al inicio", greenPlayer.isShieldActive());
    }

    @Test
    public void testInvincibilityAfterDeath() {
        player1.resetForLevel(new Position(1, 1));
        player1.handleEnemyCollision(); // muere
        assertTrue("Es invencible tras morir", player1.isInvincible());
        // Colision durante invencibilidad no mata
        boolean died = player1.handleEnemyCollision();
        assertFalse("Invencible absorbe golpe sin morir", died);
    }

    @Test
    public void testInvincibilityExpires() {
        player1.resetForLevel(new Position(1, 1));
        player1.handleEnemyCollision();
        assertTrue(player1.isInvincible());
        for (int i = 0; i <= 30; i++) {
            player1.tick();
        }
        assertFalse("Invincibilidad expiro", player1.isInvincible());
    }

    @Test
    public void testGreenPlayerRegainsShieldAfterDeath() {
        Player greenPlayer = new Player("Green", PlayerType.GREEN, new Position(1, 1));
        // Absorbe primer golpe -> pierde escudo
        greenPlayer.handleEnemyCollision();
        assertFalse("Escudo consumido", greenPlayer.isShieldActive());
        // Hacer ticks para salir de invencibilidad
        for (int i = 0; i <= 30; i++) {
            greenPlayer.tick();
        }
        // Recibe segundo golpe -> muere -> resetea con escudo de vuelta
        greenPlayer.handleEnemyCollision();
        assertTrue("GREEN recupera escudo al morir", greenPlayer.isShieldActive());
    }

    @Test
    public void testBombKillsPlayerIgnoringShield() {
        player1.resetForLevel(new Position(1, 1));
        player1.activateShield();
        assertTrue(player1.isShieldActive());
        player1.handleBombCollision();
        assertEquals("Bomba mata ignorando escudo", 1, player1.getDeaths());
    }

    @Test
    public void testBombDoesNothingWhenInvincible() {
        player1.resetForLevel(new Position(1, 1));
        player1.handleEnemyCollision(); // muere -> queda invencible
        int deathsBefore = player1.getDeaths();
        player1.handleBombCollision(); // invencible -> no hace nada
        assertEquals("Bomba no mata cuando invencible", deathsBefore, player1.getDeaths());
    }

    // ==========================================
    // 6. PRUEBAS DE COLISIONES
    // ==========================================

    @Test
    public void testPlayerEnemyCollision() {
        player1.resetForLevel(new Position(1, 1));
        Enemy enemy = new BasicEnemy(new Position(1, 1), Direction.RIGHT);
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);
        assertTrue("Hay colision", collisionDetector.checkPlayerEnemyCollision(player1, enemies));

        enemies.clear();
        enemies.add(new BasicEnemy(new Position(1, 2), Direction.RIGHT));
        assertFalse("No hay colision", collisionDetector.checkPlayerEnemyCollision(player1, enemies));
    }

    @Test
    public void testPlayerEnemyCollisionEmptyList() {
        player1.resetForLevel(new Position(1, 1));
        assertFalse("Sin enemigos: no hay colision",
                collisionDetector.checkPlayerEnemyCollision(player1, new ArrayList<>()));
    }

    @Test
    public void testPlayerCoinCollision() {
        player1.resetForLevel(new Position(1, 1));
        Coin coin = new YellowCoin(new Position(1, 1));
        List<Coin> coins = Arrays.asList(coin);
        collisionDetector.checkPlayerCoinCollision(player1, coins);
        assertTrue("La moneda se recoge", coin.isCollected());
        assertEquals("Jugador suma moneda", 1, player1.getCoinsCollected());
    }

    @Test
    public void testPlayerCoinCollisionAlreadyCollected() {
        player1.resetForLevel(new Position(1, 1));
        Coin coin = new YellowCoin(new Position(1, 1));
        coin.setCollected(true);
        int before = player1.getCoinsCollected();
        collisionDetector.checkPlayerCoinCollision(player1, Arrays.asList(coin));
        assertEquals("Moneda ya recogida no suma", before, player1.getCoinsCollected());
    }

    @Test
    public void testSafeZoneDetection() {
        player1.resetForLevel(new Position(1, 2));
        Cell currentCell = collisionDetector.checkPlayerSafeZone(player1, board);
        assertEquals("Detecto la zona final", Cell.SAFE_FINAL, currentCell);
    }

    @Test
    public void testSafeZoneIntermediate() {
        player1.resetForLevel(new Position(2, 1));
        Cell result = collisionDetector.checkPlayerSafeZone(player1, board);
        assertEquals("Zona intermedia", Cell.SAFE_INTERMEDIATE, result);
        assertEquals("Respawn actualizado", new Position(2, 1), player1.getRespawnPoint());
    }

    @Test
    public void testSafeZoneNone() {
        player1.resetForLevel(new Position(1, 1)); // EMPTY
        Cell result = collisionDetector.checkPlayerSafeZone(player1, board);
        assertNull("Sin zona segura retorna null", result);
    }

    @Test
    public void testPlayerPlayerCollision() {
        Player a = new Player("A", PlayerType.RED, new Position(3, 3));
        Player b = new Player("B", PlayerType.BLUE, new Position(3, 3));
        assertTrue("P1 y P2 colisionan", collisionDetector.checkPlayerPlayerCollision(a, b));

        Player c = new Player("C", PlayerType.RED, new Position(3, 4));
        assertFalse("P1 y C no colisionan", collisionDetector.checkPlayerPlayerCollision(a, c));
    }

    @Test
    public void testCheckPlayerSpecialElementCollisions() {
        player1.resetForLevel(new Position(5, 5));
        LifeSource ls = new LifeSource(new Position(5, 5));
        assertFalse("No activado aun", player1.isShieldActive());
        collisionDetector.checkPlayerSpecialElementCollisions(player1, Arrays.asList(ls));
        assertTrue("LifeSource activo escudo", player1.isShieldActive());
        assertTrue("LifeSource marcado como usado", ls.isUsed());
    }

    @Test
    public void testCheckPlayerSpecialElementUsed() {
        player1.resetForLevel(new Position(5, 5));
        LifeSource ls = new LifeSource(new Position(5, 5));
        ls.onPlayerCollision(player1); // ya usado
        boolean shieldBefore = player1.isShieldActive();
        collisionDetector.checkPlayerSpecialElementCollisions(player1, Arrays.asList(ls));
        assertEquals("No vuelve a activarse", shieldBefore, player1.isShieldActive());
    }

    @Test
    public void testCheckEnemySpecialElementCollisions() {
        BasicEnemy enemy = new BasicEnemy(new Position(1, 1), Direction.RIGHT);
        Bomb bomb = new Bomb(new Position(1, 1));
        collisionDetector.checkEnemySpecialElementCollisions(
                Arrays.asList(enemy), Arrays.asList(bomb));
        assertTrue("Bomba usada", bomb.isUsed());
        assertEquals("Enemigo reseteado", new Position(1, 1), enemy.getPosition());
    }

    // ==========================================
    // 7. PRUEBAS DE MONEDAS
    // ==========================================

    @Test
    public void testYellowCoinCollect() {
        YellowCoin coin = new YellowCoin(new Position(2, 2));
        assertEquals(CoinType.YELLOW, coin.getType());
        assertFalse(coin.isCollected());
        coin.onCollect(player1);
        assertTrue(coin.isCollected());
        assertEquals(1, player1.getCoinsCollected());
    }

    @Test
    public void testCoinReset() {
        YellowCoin coin = new YellowCoin(new Position(1, 1));
        coin.onCollect(player1);
        assertTrue(coin.isCollected());
        coin.reset();
        assertFalse("Coin reseteada", coin.isCollected());
    }

    @Test
    public void testCoinSetCollected() {
        YellowCoin coin = new YellowCoin(new Position(1, 1));
        coin.setCollected(true);
        assertTrue(coin.isCollected());
        coin.setCollected(false);
        assertFalse(coin.isCollected());
    }

    @Test
    public void testCoinToString() {
        YellowCoin coin = new YellowCoin(new Position(3, 4));
        String s = coin.toString();
        assertNotNull(s);
        assertTrue(s.contains("YELLOW"));
    }

    @Test
    public void testSkinCoinRedEffect() {
        SkinCoin coin = new SkinCoin(new Position(1, 1), CoinType.RED_SKIN);
        assertEquals(CoinType.RED_SKIN, coin.getType());
        player1.applySkinEffect(PlayerType.GREEN); // Cambia a GREEN antes
        coin.onCollect(player1);
        assertTrue(coin.isCollected());
        assertEquals(PlayerType.RED, player1.getType());
    }

    @Test
    public void testSkinCoinBlueEffect() {
        SkinCoin coin = new SkinCoin(new Position(1, 1), CoinType.BLUE_SKIN);
        float before = player1.getSpeed();
        coin.onCollect(player1);
        assertTrue("Blue skin aumenta velocidad", player1.getSpeed() > before);
        assertEquals(PlayerType.BLUE, player1.getType());
    }

    @Test
    public void testSkinCoinGreenEffect() {
        SkinCoin coin = new SkinCoin(new Position(1, 1), CoinType.GREEN_SKIN);
        coin.onCollect(player1);
        assertEquals(PlayerType.GREEN, player1.getType());
        assertTrue("Green skin da escudo", player1.isShieldActive());
    }

    @Test
    public void testCollisionDetectorResetsSkinOnSkinCoin() {
        player1.resetForLevel(new Position(1, 1));
        player1.applySkinEffect(PlayerType.BLUE);
        SkinCoin coin = new SkinCoin(new Position(1, 1), CoinType.GREEN_SKIN);
        // CollisionDetector resetea skin antes de aplicar la nueva
        collisionDetector.checkPlayerCoinCollision(player1, Arrays.asList(coin));
        assertEquals("Nueva skin aplicada", PlayerType.GREEN, player1.getType());
    }

    // ==========================================
    // 8. PRUEBAS DE ELEMENTOS ESPECIALES
    // ==========================================

    @Test
    public void testLifeSourceGivesShield() {
        player1.resetForLevel(new Position(5, 5));
        assertFalse(player1.isShieldActive());
        LifeSource ls = new LifeSource(new Position(5, 5));
        ls.onPlayerCollision(player1);
        assertTrue("LifeSource da escudo", player1.isShieldActive());
        assertTrue("LifeSource usada", ls.isUsed());
    }

    @Test
    public void testLifeSourceDoesNothingWhenUsed() {
        player1.resetForLevel(new Position(5, 5));
        LifeSource ls = new LifeSource(new Position(5, 5));
        ls.onPlayerCollision(player1); // primer uso: used=true
        assertTrue("Esta usada", ls.isUsed());
        // Segundo intento: no debe lanzar excepcion y sigue usada
        ls.onPlayerCollision(player1);
        assertTrue("Sigue siendo usada", ls.isUsed());
    }

    @Test
    public void testLifeSourceIgnoresEnemy() {
        LifeSource ls = new LifeSource(new Position(3, 3));
        BasicEnemy enemy = new BasicEnemy(new Position(3, 3), Direction.RIGHT);
        ls.onEnemyCollision(enemy); // no lanza excepcion, enemigos no afectados
        assertFalse("LifeSource no se usa con enemigo", ls.isUsed());
    }

    @Test
    public void testLifeSourceGetPosition() {
        LifeSource ls = new LifeSource(new Position(4, 5));
        assertEquals(new Position(4, 5), ls.getPosition());
    }

    @Test
    public void testLifeSourceReset() {
        Player p = new Player("T", PlayerType.RED, new Position(1, 1));
        LifeSource ls = new LifeSource(new Position(1, 1));
        ls.onPlayerCollision(p);
        assertTrue(ls.isUsed());
        ls.reset();
        assertFalse("LifeSource reseteada", ls.isUsed());
    }

    @Test
    public void testBombKillsPlayer() {
        player1.resetForLevel(new Position(3, 3));
        Bomb bomb = new Bomb(new Position(3, 3));
        bomb.onPlayerCollision(player1);
        assertEquals("Bomba mata al jugador", 1, player1.getDeaths());
        assertTrue("Bomba usada", bomb.isUsed());
    }

    @Test
    public void testBombResetsEnemyOnCollision() {
        BasicEnemy enemy = new BasicEnemy(new Position(2, 2), Direction.RIGHT);
        enemy.update(board); // mueve enemigo
        Bomb bomb = new Bomb(new Position(2, 2));
        bomb.onEnemyCollision(enemy);
        assertEquals("Enemigo reseteado por bomba", new Position(2, 2), enemy.getPosition());
        assertTrue("Bomba usada", bomb.isUsed());
    }

    @Test
    public void testBombDoesNothingWhenAlreadyUsed() {
        player1.resetForLevel(new Position(3, 3));
        Bomb bomb = new Bomb(new Position(3, 3));
        bomb.onPlayerCollision(player1);
        int deathsBefore = player1.getDeaths();
        bomb.onPlayerCollision(player1); // ya usada, no afecta
        assertEquals("Bomba ya usada no vuelve a matar", deathsBefore, player1.getDeaths());
    }

    @Test
    public void testBombGetPosition() {
        Bomb bomb = new Bomb(new Position(7, 8));
        assertEquals(new Position(7, 8), bomb.getPosition());
    }

    @Test
    public void testBombReset() {
        Player p = new Player("T", PlayerType.RED, new Position(1, 1));
        Bomb bomb = new Bomb(new Position(1, 1));
        bomb.onPlayerCollision(p);
        assertTrue(bomb.isUsed());
        bomb.reset();
        assertFalse("Bomba reseteada", bomb.isUsed());
    }

    // ==========================================
    // 9. PRUEBAS DE ENEMIGOS
    // ==========================================

    @Test
    public void testBasicEnemyMovement() {
        // Tablero de 1x5: [EMPTY, EMPTY, EMPTY, EMPTY, EMPTY]
        Cell[][] g = { { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY, Cell.EMPTY, Cell.EMPTY } };
        Board b = new Board(g);
        BasicEnemy e = new BasicEnemy(new Position(0, 0), Direction.RIGHT);
        e.update(b); // tick 1
        e.update(b); // tick 2
        e.update(b); // tick 3 -> se mueve
        assertEquals("Enemigo se movio", new Position(0, 1), e.getPosition());
    }

    @Test
    public void testBasicEnemyBounce() {
        Cell[][] g = { { Cell.WALL, Cell.EMPTY, Cell.WALL } };
        Board b = new Board(g);
        BasicEnemy e = new BasicEnemy(new Position(0, 1), Direction.RIGHT);
        // Forzar movimiento directo
        for (int i = 0; i < 10; i++)
            e.update(b);
        // El enemigo debe quedarse en la franja EMPTY o rebotar
        assertTrue("Enemigo en columna valida",
                e.getPosition().getCol() >= 1 && e.getPosition().getCol() <= 1);
    }

    @Test
    public void testBasicEnemyReset() {
        Cell[][] g = { { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY, Cell.EMPTY, Cell.EMPTY } };
        Board b = new Board(g);
        BasicEnemy e = new BasicEnemy(new Position(0, 0), Direction.RIGHT);
        for (int i = 0; i < 9; i++)
            e.update(b); // mueve
        e.reset();
        assertEquals("Posicion inicial", new Position(0, 0), e.getPosition());
        assertEquals("Direccion inicial", Direction.RIGHT, e.getCurrentDirection());
    }

    @Test
    public void testBasicEnemyGetters() {
        BasicEnemy e = new BasicEnemy(new Position(3, 4), Direction.LEFT);
        assertEquals(new Position(3, 4), e.getPosition());
        assertEquals(new Position(3, 4), e.getInitialPosition());
        assertEquals(1.0f, e.getSpeed(), 0.001f);
        assertEquals(Direction.LEFT, e.getCurrentDirection());
        assertNotNull(e.toString());
    }

    @Test
    public void testFastEnemyMovement() {
        Cell[][] g = { { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY, Cell.EMPTY, Cell.EMPTY } };
        Board b = new Board(g);
        FastEnemy e = new FastEnemy(new Position(0, 0), Direction.RIGHT);
        // speed=2 -> ticksPerMove = max(1, round(3/2)) = 2, necesita 2 ticks para
        // moverse
        e.update(b); // tick 1 - no mueve
        e.update(b); // tick 2 - mueve
        assertEquals("FastEnemy avanza mas rapido que Basic", new Position(0, 1), e.getPosition());
    }

    @Test
    public void testFastEnemyReset() {
        Cell[][] g = { { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY } };
        Board b = new Board(g);
        FastEnemy e = new FastEnemy(new Position(0, 0), Direction.RIGHT);
        for (int i = 0; i < 5; i++)
            e.update(b);
        e.reset();
        assertEquals(new Position(0, 0), e.getPosition());
        assertEquals(Direction.RIGHT, e.getCurrentDirection());
    }

    @Test
    public void testPatrolEnemyFollowsWaypoints() {
        Cell[][] g = {
                { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY },
                { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY },
                { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY }
        };
        Board b = new Board(g);
        List<Position> waypoints = Arrays.asList(
                new Position(0, 0), new Position(0, 2), new Position(2, 2));
        PatrolEnemy e = new PatrolEnemy(new Position(0, 0), waypoints);
        // Hacer ticks para que avance
        for (int i = 0; i < 30; i++)
            e.update(b);
        // Solo verificamos que se mueve y no lanza excepciones
        assertNotNull(e.getPosition());
        assertEquals(3, e.getWaypoints().size());
    }

    @Test
    public void testPatrolEnemyReset() {
        List<Position> waypoints = Arrays.asList(
                new Position(1, 0), new Position(1, 2));
        Cell[][] g = {
                { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY },
                { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY }
        };
        Board b = new Board(g);
        PatrolEnemy e = new PatrolEnemy(new Position(1, 0), waypoints);
        for (int i = 0; i < 20; i++)
            e.update(b);
        e.reset();
        assertEquals("PatrolEnemy reseteado", new Position(1, 0), e.getPosition());
    }

    @Test
    public void testPatrolEnemyEmptyWaypoints() {
        Cell[][] g = { { Cell.EMPTY, Cell.EMPTY } };
        Board b = new Board(g);
        PatrolEnemy e = new PatrolEnemy(new Position(0, 0), new ArrayList<>());
        e.update(b); // no lanza excepcion con waypoints vacios
        assertEquals(new Position(0, 0), e.getPosition());
    }

    // ==========================================
    // 10. PRUEBAS DEL TEMPORIZADOR (GameTimer)
    // ==========================================

    @Test
    public void testGameTimerInitial() {
        GameTimer timer = new GameTimer();
        assertFalse("No esta corriendo", timer.isRunning());
        assertFalse("No esta pausado", timer.isPaused());
        assertEquals("Tiempo inicial", GameTimer.DEFAULT_TIME_LIMIT, timer.getRemainingSeconds());
        assertEquals(0, timer.getElapsedMillis());
    }

    @Test
    public void testGameTimerStart() {
        GameTimer timer = new GameTimer(60);
        timer.start();
        assertTrue("Corriendo", timer.isRunning());
        assertFalse("No pausado", timer.isPaused());
        assertTrue("Segundos restantes <= 60", timer.getRemainingSeconds() <= 60);
        assertTrue("Tiempo transcurrido >= 0", timer.getElapsedMillis() >= 0);
    }

    @Test
    public void testGameTimerPauseResume() {
        GameTimer timer = new GameTimer(60);
        timer.start();
        timer.pause();
        assertTrue("Pausado", timer.isPaused());
        long elapsed = timer.getElapsedMillis();
        // Mientras pausado el tiempo no avanza
        long elapsed2 = timer.getElapsedMillis();
        assertEquals("Tiempo congelado en pausa", elapsed, elapsed2);
        timer.resume();
        assertFalse("Reanudado: no pausado", timer.isPaused());
    }

    @Test
    public void testGameTimerStop() {
        GameTimer timer = new GameTimer(60);
        timer.start();
        timer.stop();
        assertFalse("Detenido: no corriendo", timer.isRunning());
        assertFalse("Detenido: no pausado", timer.isPaused());
    }

    @Test
    public void testGameTimerExpired() {
        GameTimer timer = new GameTimer(0); // limite 0 segundos
        timer.start();
        assertTrue("Expirado", timer.isExpired());
    }

    @Test
    public void testGameTimerNotExpiredWhenStopped() {
        GameTimer timer = new GameTimer(60);
        assertFalse("No expirado cuando no corre", timer.isExpired());
    }

    @Test
    public void testGameTimerFormattedTime() {
        GameTimer timer = new GameTimer(125); // 2:05
        String formatted = timer.getFormattedTime();
        assertNotNull(formatted);
        assertTrue("Formato MM:SS", formatted.contains(":"));
        assertEquals("2:05", formatted);
    }

    @Test
    public void testGameTimerSetTimeLimit() {
        GameTimer timer = new GameTimer(60);
        timer.setTimeLimit(120);
        assertEquals(120, timer.getRemainingSeconds());
    }

    @Test
    public void testGameTimerRestoreState() {
        GameTimer timer = new GameTimer(60);
        timer.restoreState(120, 30000, false, false);
        assertFalse(timer.isRunning());
        assertEquals(120, timer.getRemainingSeconds());
    }

    @Test
    public void testGameTimerPauseIdempotent() {
        GameTimer timer = new GameTimer(60);
        timer.start();
        timer.pause();
        long before = timer.getElapsedMillis();
        timer.pause(); // segunda pausa: no cambia nada
        long after = timer.getElapsedMillis();
        assertEquals(before, after);
    }

    @Test
    public void testGameTimerResumeNotPaused() {
        GameTimer timer = new GameTimer(60);
        timer.start();
        // resume sin pausar: no cambia estado
        timer.resume();
        assertFalse("No esta pausado", timer.isPaused());
    }

    // ==========================================
    // 11. PRUEBAS DE REGLAS DE JUEGO (GAME)
    // ==========================================

    @Test
    public void testGetWinner() {
        List<Player> players = Arrays.asList(player1, player2);
        Game game = new Game(GameMode.PLAYER_VS_PLAYER, players, new ArrayList<>(), null);

        player1.addLevelWin();
        assertEquals("Gana P1 por niveles", player1, game.getWinner());

        player2.addLevelWin();
        player1.handleEnemyCollision();
        assertEquals("Gana P2 por menos muertes", player2, game.getWinner());

        player2.handleEnemyCollision();
        player1.setLevelGoalReachedTimeMs(1000);
        player1.addLevelTime();
        player2.setLevelGoalReachedTimeMs(2000);
        player2.addLevelTime();
        assertEquals("Gana P1 por mejor tiempo acumulado", player1, game.getWinner());
    }

    @Test
    public void testGetWinnerTie() {
        List<Player> players = Arrays.asList(player1, player2);
        Game game = new Game(GameMode.PLAYER_VS_PLAYER, players, new ArrayList<>(), null);
        assertNull("Empate absoluto: null", game.getWinner());
    }

    @Test
    public void testGetWinnerSinglePlayer() {
        List<Player> players = Arrays.asList(player1);
        Game game = new Game(GameMode.SINGLE_PLAYER, players, new ArrayList<>(), null);
        assertNull("SinglePlayer no tiene ganador", game.getWinner());
    }

    @Test
    public void testGameStateInitial() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        assertEquals(GameState.MENU, game.getState());
    }

    @Test
    public void testGameGetMode() {
        Game game = new Game(GameMode.PLAYER_VS_MACHINE, Arrays.asList(player1), new ArrayList<>(), null);
        assertEquals(GameMode.PLAYER_VS_MACHINE, game.getMode());
    }

    @Test
    public void testGameGetPlayers() {
        List<Player> players = Arrays.asList(player1, player2);
        Game game = new Game(GameMode.PLAYER_VS_PLAYER, players, new ArrayList<>(), null);
        assertEquals(2, game.getPlayers().size());
    }

    @Test
    public void testGameGetTimer() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        assertNotNull(game.getTimer());
    }

    @Test
    public void testGameGetAIProfile() {
        Game game = new Game(GameMode.PLAYER_VS_MACHINE, Arrays.asList(player1),
                new ArrayList<>(), AIProfile.RANDOM);
        assertEquals(AIProfile.RANDOM, game.getAIProfile());
    }

    @Test
    public void testGameSetState() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        game.setState(GameState.PAUSED);
        assertEquals(GameState.PAUSED, game.getState());
    }

    @Test
    public void testGameSetGameListener() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        // Debe poder setear y notificar sin excepcion
        game.setGameListener(state -> {
        });
        game.setState(GameState.PLAYING);
    }

    @Test
    public void testGamePauseAndResume() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        game.setState(GameState.PLAYING);
        game.getTimer().start();
        game.pause();
        assertEquals(GameState.PAUSED, game.getState());
        game.resume();
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test
    public void testGamePauseOnlyWhenPlaying() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        game.setState(GameState.MENU);
        game.pause(); // no cambia nada
        assertEquals(GameState.MENU, game.getState());
    }

    @Test
    public void testGameResumeOnlyWhenPaused() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        game.setState(GameState.PLAYING);
        game.resume(); // no cambia nada
        assertEquals(GameState.PLAYING, game.getState());
    }

    @Test
    public void testGameQuit() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        game.setState(GameState.PLAYING);
        game.quit();
        assertEquals(GameState.MENU, game.getState());
    }

    @Test
    public void testGameTickDoesNothingWhenNotPlaying() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        game.setState(GameState.PAUSED);
        game.tick(); // no debe lanzar excepcion
        assertEquals(GameState.PAUSED, game.getState());
    }

    @Test
    public void testGameMovePlayerOnlyWhenPlaying() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        game.setState(GameState.PAUSED);
        Position before = player1.getPosition();
        game.movePlayer(0, Direction.RIGHT);
        assertEquals("No se mueve cuando no juega", before, player1.getPosition());
    }

    @Test
    public void testGameGetTotalLevels() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        assertEquals(0, game.getTotalLevels());
    }

    @Test
    public void testGameGoalCellAssignment() {
        List<Player> players = Arrays.asList(player1, player2);
        Game game = new Game(GameMode.PLAYER_VS_PLAYER, players, new ArrayList<>(), null);
        assertEquals("P1 apunta a SAFE_FINAL", Cell.SAFE_FINAL, player1.getGoalCell());
        assertEquals("P2 apunta a START", Cell.START, player2.getGoalCell());
    }

    @Test
    public void testGameGoalCellSinglePlayer() {
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), new ArrayList<>(), null);
        assertEquals("P1 en solo: SAFE_FINAL", Cell.SAFE_FINAL, player1.getGoalCell());
    }

    // ==========================================
    // 12. PRUEBAS DE GAMEEXCEPTION
    // ==========================================

    @Test
    public void testGameException() {
        GameException ex = new GameException("Error de prueba");
        assertEquals("Error de prueba", ex.getMessage());
    }

    @Test
    public void testGameExceptionWithCause() {
        Throwable cause = new RuntimeException("causa");
        GameException ex = new GameException("Error con causa", cause);
        assertEquals("Error con causa", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    // ==========================================
    // 13. PRUEBAS DE PLAYERTYPE
    // ==========================================

    @Test
    public void testPlayerTypeAttributes() {
        assertEquals(1.0f, PlayerType.RED.getBaseSpeed(), 0.001f);
        assertFalse("RED sin escudo", PlayerType.RED.hasShield());
        assertNotNull(PlayerType.RED.getColor());
        assertEquals(20, PlayerType.RED.getSize());

        assertEquals(1.5f, PlayerType.BLUE.getBaseSpeed(), 0.001f);
        assertFalse("BLUE sin escudo", PlayerType.BLUE.hasShield());
        assertEquals(30, PlayerType.BLUE.getSize());

        assertEquals(1.0f, PlayerType.GREEN.getBaseSpeed(), 0.001f);
        assertTrue("GREEN con escudo", PlayerType.GREEN.hasShield());
    }

    // ==========================================
    // 14. PRUEBAS DE CELL
    // ==========================================

    @Test
    public void testCellWalkable() {
        assertTrue(Cell.EMPTY.isWalkable());
        assertTrue(Cell.START.isWalkable());
        assertTrue(Cell.SAFE_FINAL.isWalkable());
        assertTrue(Cell.SAFE_INTERMEDIATE.isWalkable());
        assertFalse(Cell.WALL.isWalkable());
    }

    // ==========================================
    // 15. PRUEBAS DE VERTICALENEMY
    // ==========================================

    @Test
    public void testVerticalEnemyInitialization() {
        VerticalEnemy enemyUp = new VerticalEnemy(new Position(5, 5), Direction.UP);
        assertEquals(new Position(5, 5), enemyUp.getPosition());
        
        VerticalEnemy enemyInvalid = new VerticalEnemy(new Position(5, 5), Direction.RIGHT);
        assertEquals(new Position(5, 5), enemyInvalid.getPosition());
    }

    @Test
    public void testVerticalEnemyMovementWalkable() {
        Cell[][] grid = {
                { Cell.EMPTY },
                { Cell.EMPTY },
                { Cell.EMPTY }
        };
        Board b = new Board(grid);
        VerticalEnemy enemy = new VerticalEnemy(new Position(0, 0), Direction.DOWN);
        
        for (int i = 0; i < 3; i++) enemy.update(b);
        assertEquals(new Position(1, 0), enemy.getPosition());
        
        for (int i = 0; i < 3; i++) enemy.update(b);
        assertEquals(new Position(2, 0), enemy.getPosition());
    }

    @Test
    public void testVerticalEnemyMovementBounce() {
        Cell[][] grid = {
                { Cell.WALL },
                { Cell.EMPTY },
                { Cell.EMPTY }
        };
        Board b = new Board(grid);
        VerticalEnemy enemy = new VerticalEnemy(new Position(1, 0), Direction.UP);
        for (int i = 0; i < 3; i++) enemy.update(b);
        assertEquals("Rebota a DOWN y se mueve a 2,0", new Position(2, 0), enemy.getPosition());
    }

    @Test
    public void testVerticalEnemyReset() {
        Cell[][] grid = {
                { Cell.EMPTY },
                { Cell.EMPTY },
                { Cell.EMPTY }
        };
        Board b = new Board(grid);
        VerticalEnemy enemy = new VerticalEnemy(new Position(0, 0), Direction.DOWN);
        for (int i = 0; i < 3; i++) enemy.update(b);
        assertEquals(new Position(1, 0), enemy.getPosition());
        enemy.reset();
        assertEquals("Resetea a posicion inicial", new Position(0, 0), enemy.getPosition());
    }

    // ==========================================
    // 16. PRUEBAS DE SAFEZONE
    // ==========================================

    @Test
    public void testSafeZoneClassIntermediate() {
        Position pos = new Position(3, 4);
        SafeZone sz = new SafeZone(pos, true);
        assertTrue(sz.isIntermediate());
        assertFalse(sz.isFinal());
        assertEquals(pos, sz.getPosition());
        assertEquals("Intermedia SafeZone en (3, 4)", sz.toString());
    }

    @Test
    public void testSafeZoneClassFinal() {
        Position pos = new Position(1, 2);
        SafeZone sz = new SafeZone(pos, false);
        assertFalse(sz.isIntermediate());
        assertTrue(sz.isFinal());
        assertEquals(pos, sz.getPosition());
        assertEquals("Final SafeZone en (1, 2)", sz.toString());
    }

    // ==========================================
    // 17. PRUEBAS DE RANDOMAICONTROLLER
    // ==========================================

    @Test
    public void testRandomAIControllerGetNextDirection() throws GameException {
        RandomAIController ai = new RandomAIController();
        Player p = new Player("AIPlayer", PlayerType.RED, new Position(1, 1));
        
        Cell[][] grid = {
            { Cell.WALL, Cell.WALL, Cell.WALL },
            { Cell.WALL, Cell.EMPTY, Cell.WALL },
            { Cell.WALL, Cell.WALL, Cell.WALL }
        };
        LevelConfig config = new LevelConfig(
            "TestLevel",
            grid,
            new Position(1, 1),
            null,
            60,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        Level level = new Level(config);
        
        Direction dir = ai.getNextDirection(p, level);
        assertNull("Rodeado de paredes: retorna null", dir);
        
        Cell[][] gridOpen = {
            { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY },
            { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY },
            { Cell.EMPTY, Cell.EMPTY, Cell.EMPTY }
        };
        LevelConfig configOpen = new LevelConfig(
            "OpenLevel",
            gridOpen,
            new Position(1, 1),
            null,
            60,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
        Level levelOpen = new Level(configOpen);
        
        Direction dirOpen = ai.getNextDirection(p, levelOpen);
        assertNotNull("Hay direcciones transitables", dirOpen);
        assertTrue("Direccion valida", Arrays.asList(Direction.cardinals()).contains(dirOpen));
        
        p.setReachedGoal(true);
        Direction dirReached = ai.getNextDirection(p, levelOpen);
        assertNull("Ya llego a la meta: retorna null", dirReached);
    }

    // ==========================================
    // 18. PRUEBAS DE PLAYER RESTORESTATE
    // ==========================================

    @Test
    public void testPlayerRestoreState() {
        Player p = new Player("TestPlayer", PlayerType.RED, new Position(0, 0));
        Position pos = new Position(2, 3);
        Position respawn = new Position(1, 1);
        p.restoreState(pos, respawn, 5, 2.0f, true,
                3, true, 10, true,
                1000L, 5000L, 2, java.awt.Color.BLUE, Cell.SAFE_FINAL, PlayerType.BLUE);

        assertEquals(pos, p.getPosition());
        assertEquals(respawn, p.getRespawnPoint());
        assertEquals(5, p.getDeaths());
        assertEquals(2.0f, p.getSpeed(), 0.001f);
        assertTrue(p.isShieldActive());
        assertEquals(3, p.getCoinsCollected());
        assertTrue(p.isInvincible());
        assertTrue(p.hasReachedGoal());
        assertEquals(1000L, p.getLevelGoalReachedTimeMs());
        assertEquals(5000L, p.getTotalGoalReachedTimeMs());
        assertEquals(2, p.getLevelWins());
        assertEquals(Cell.SAFE_FINAL, p.getGoalCell());
        assertEquals(PlayerType.BLUE, p.getType());
        assertEquals(java.awt.Color.BLUE, p.getBorderColor());
    }

    // ==========================================
    // 19. PRUEBAS ADICIONALES DE GAME
    // ==========================================

    @Test
    public void testGameStartGame() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), Arrays.asList(config), null);
        
        assertEquals(GameState.MENU, game.getState());
        game.startGame();
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(0, game.getCurrentLevelIndex());
        assertNotNull(game.getCurrentLevel());
        assertTrue(game.getTimer().isRunning());
    }

    @Test
    public void testGameStartGameException() {
        Cell[][] grid = { { Cell.WALL, Cell.EMPTY } }; // startPosition no transitable
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), Arrays.asList(config), null);
        game.startGame();
        assertEquals(GameState.GAME_OVER, game.getState());
    }

    @Test
    public void testGameNextLevelAndVictory() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig config1 = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        LevelConfig config2 = new LevelConfig(
            "L2", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), Arrays.asList(config1, config2), null);
        
        game.startGame();
        assertEquals(0, game.getCurrentLevelIndex());
        
        game.nextLevel();
        assertEquals(1, game.getCurrentLevelIndex());
        assertEquals(GameState.PLAYING, game.getState());
        
        game.nextLevel();
        assertEquals(GameState.VICTORY, game.getState());
        assertFalse(game.getTimer().isRunning());
    }

    @Test
    public void testGameNextLevelException() {
        Cell[][] gridOk = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        Cell[][] gridBad = { { Cell.WALL, Cell.EMPTY } };
        LevelConfig config1 = new LevelConfig(
            "L1", gridOk, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        LevelConfig config2 = new LevelConfig(
            "L2", gridBad, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), Arrays.asList(config1, config2), null);
        
        game.startGame();
        game.nextLevel();
        assertEquals(GameState.GAME_OVER, game.getState());
    }

    @Test
    public void testGameMovePlayerSuccess() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), Arrays.asList(config), null);
        game.startGame();
        
        Position before = player1.getPosition();
        game.movePlayer(0, Direction.RIGHT);
        assertNotEquals(before, player1.getPosition());
        
        game.movePlayer(99, Direction.RIGHT);
        game.movePlayer(-1, Direction.RIGHT);
    }

    @Test
    public void testGameLoadLevelForRestore() throws GameException {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig config1 = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        LevelConfig config2 = new LevelConfig(
            "L2", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), Arrays.asList(config1, config2), null);
        
        game.loadLevelForRestore(1);
        assertEquals(1, game.getCurrentLevelIndex());
        assertEquals("L2", game.getCurrentLevel().getName());
        
        try {
            game.loadLevelForRestore(5);
            fail("Debería lanzar GameException");
        } catch (GameException e) {
            // ok
        }
        try {
            game.loadLevelForRestore(-1);
            fail("Debería lanzar GameException");
        } catch (GameException e) {
            // ok
        }
    }

    @Test
    public void testGameTickTimerExpired() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 0,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(player1), Arrays.asList(config), null);
        game.startGame();
        
        assertTrue(game.getTimer().isExpired());
        game.tick();
        
        assertEquals(GameState.GAME_OVER, game.getState());
        assertFalse(game.getTimer().isRunning());
    }

    @Test
    public void testGameTickPlayerPlayerCollision() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Player p1 = new Player("P1", PlayerType.RED, new Position(0, 0));
        Player p2 = new Player("P2", PlayerType.BLUE, new Position(0, 0));
        
        Game game = new Game(GameMode.PLAYER_VS_PLAYER, Arrays.asList(p1, p2), Arrays.asList(config), null);
        game.startGame();
        
        assertEquals(0, p1.getDeaths());
        assertEquals(0, p2.getDeaths());
        
        game.tick();
        
        assertEquals(1, p1.getDeaths());
        assertEquals(1, p2.getDeaths());
    }

    @Test
    public void testGameTickMachineAI() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
        Player p1 = new Player("P1", PlayerType.RED, new Position(0, 0));
        Player p2 = new Player("P2", PlayerType.BLUE, new Position(0, 0));
        
        Game game = new Game(GameMode.PLAYER_VS_MACHINE, Arrays.asList(p1, p2), Arrays.asList(config), AIProfile.RANDOM);
        game.startGame();
        
        for (int i = 0; i < 10; i++) {
            p2.tick();
        }
        
        game.tick();
        assertEquals(new Position(0, 1), p2.getPosition());
    }

    @Test
    public void testGameTickPlayerEnemyCollisionWithAndWithoutShield() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig.EnemyConfig ec = new LevelConfig.EnemyConfig(
            LevelConfig.EnemyType.BASIC, new Position(0, 0), Direction.RIGHT
        );
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), Arrays.asList(ec), new ArrayList<>()
        );
        
        Player p1 = new Player("P1", PlayerType.RED, new Position(0, 0));
        
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(p1), Arrays.asList(config), null);
        game.startGame();
        p1.activateShield();
        
        assertEquals(new Position(0, 0), game.getCurrentLevel().getEnemies().get(0).getPosition());
        assertTrue(p1.isShieldActive());
        assertEquals(0, p1.getDeaths());
        
        game.tick();
        assertFalse(p1.isShieldActive());
        assertEquals(0, p1.getDeaths());
        
        for (int i = 0; i < 40; i++) {
            p1.tick();
        }
        
        game.tick();
        assertEquals(1, p1.getDeaths());
    }

    @Test
    public void testGameTickPlayerCoinCollision() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig.CoinConfig cc = new LevelConfig.CoinConfig(
            CoinType.YELLOW, new Position(0, 0)
        );
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            Arrays.asList(cc), new ArrayList<>(), new ArrayList<>()
        );
        Player p = new Player("P", PlayerType.RED, new Position(0, 0));
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(p), Arrays.asList(config), null);
        game.startGame();
        
        assertEquals(0, p.getCoinsCollected());
        assertFalse(game.getCurrentLevel().getCoins().get(0).isCollected());
        
        game.tick();
        
        assertEquals(1, p.getCoinsCollected());
        assertTrue(game.getCurrentLevel().getCoins().get(0).isCollected());
    }

    @Test
    public void testGameTickPlayerSpecialElementCollision() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig.SpecialElementConfig sec = new LevelConfig.SpecialElementConfig(
            LevelConfig.SpecialElementType.LIFE_SOURCE, new Position(0, 0)
        );
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), new ArrayList<>(), Arrays.asList(sec)
        );
        Player p = new Player("P", PlayerType.RED, new Position(0, 0));
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(p), Arrays.asList(config), null);
        game.startGame();
        
        assertFalse(p.isShieldActive());
        
        game.tick();
        
        assertTrue(p.isShieldActive());
        assertTrue(game.getCurrentLevel().getSpecialElements().get(0).isUsed());
    }

    @Test
    public void testGameTickEnemySpecialElementCollision() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig.EnemyConfig ec = new LevelConfig.EnemyConfig(
            LevelConfig.EnemyType.BASIC, new Position(0, 0), Direction.RIGHT
        );
        LevelConfig.SpecialElementConfig sec = new LevelConfig.SpecialElementConfig(
            LevelConfig.SpecialElementType.BOMB, new Position(0, 0)
        );
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            new ArrayList<>(), Arrays.asList(ec), Arrays.asList(sec)
        );
        Player p = new Player("P", PlayerType.RED, new Position(0, 0));
        
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(p), Arrays.asList(config), null);
        game.startGame();
        
        // Poner el jugador en otra posición para que no muera por la bomba
        p.resetForLevel(new Position(0, 1));
        
        SpecialElement bomb = game.getCurrentLevel().getSpecialElements().get(0);
        assertFalse(bomb.isUsed());
        
        game.tick();
        
        assertTrue(bomb.isUsed());
    }

    @Test
    public void testGameTickPlayerReachesGoalAllCoinsCollected() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig.CoinConfig cc = new LevelConfig.CoinConfig(
            CoinType.YELLOW, new Position(0, 1)
        );
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            Arrays.asList(cc), new ArrayList<>(), new ArrayList<>()
        );
        Player p = new Player("P", PlayerType.RED, new Position(0, 0));
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(p), Arrays.asList(config), null);
        game.startGame();
        
        for (int i = 0; i < 10; i++) p.tick();
        game.movePlayer(0, Direction.RIGHT);
        game.tick();
        assertTrue(game.getCurrentLevel().allCoinsCollected());
        
        for (int i = 0; i < 10; i++) p.tick();
        game.movePlayer(0, Direction.RIGHT);
        
        assertEquals(0, p.getLevelWins());
        assertEquals(GameState.PLAYING, game.getState());
        
        game.tick();
        
        assertTrue(p.hasReachedGoal());
        assertEquals(1, p.getLevelWins());
        assertEquals(GameState.LEVEL_COMPLETE, game.getState());
        assertFalse(game.getTimer().isRunning());
    }

    @Test
    public void testGameTickPlayerReachesGoalCoinsRemaining() {
        Cell[][] grid = { { Cell.START, Cell.EMPTY, Cell.SAFE_FINAL } };
        LevelConfig.CoinConfig cc = new LevelConfig.CoinConfig(
            CoinType.YELLOW, new Position(0, 1)
        );
        LevelConfig config = new LevelConfig(
            "L1", grid, new Position(0, 0), null, 60,
            Arrays.asList(cc), new ArrayList<>(), new ArrayList<>()
        );
        Player p = new Player("P", PlayerType.RED, new Position(0, 0));
        Game game = new Game(GameMode.SINGLE_PLAYER, Arrays.asList(p), Arrays.asList(config), null);
        game.startGame();
        
        p.restoreState(new Position(0, 2), new Position(0, 0), 0, 1.0f, false,
                0, false, 0, true, 0, 0, 0, java.awt.Color.RED, Cell.SAFE_FINAL, PlayerType.RED);
        
        assertFalse(game.getCurrentLevel().allCoinsCollected());
        assertTrue(p.hasReachedGoal());
        
        game.tick();
        
        assertTrue(p.hasReachedGoal());
        assertEquals(GameState.PLAYING, game.getState());
        
        p.restoreState(new Position(0, 0), new Position(0, 0), 0, 1.0f, false,
                0, false, 0, true, 0, 0, 0, java.awt.Color.RED, Cell.SAFE_FINAL, PlayerType.RED);
        
        game.tick();
        
        assertFalse(p.hasReachedGoal());
    }
}
