package domain;

/**
 * Representa el tablero del juego como una grilla 2D de celdas.
 * Contiene la disposicion estatica del nivel (paredes, inicio, zonas seguras).
 * Los objetos dinamicos (jugadores, enemigos, monedas) existen por separado.
 */
import java.io.Serializable;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Cell[][] grid;
    private final int rows;
    private final int cols;

    /**
     * Crea un tablero a partir de una grilla existente.
     * Realiza una copia profunda para preservar la encapsulacion.
     * @param grid grilla de celdas a copiar
     */
    public Board(Cell[][] grid) {
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                this.grid[r][c] = grid[r][c];
            }
        }
    }

    /**
     * Crea un tablero vacio con las dimensiones dadas.
     * Todas las celdas se inicializan como EMPTY.
     * @param rows numero de filas
     * @param cols numero de columnas
     */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                this.grid[r][c] = Cell.EMPTY;
            }
        }
    }

    /**
     * Retorna la celda en las coordenadas dadas.
     * Las posiciones fuera de limites retornan WALL.
     */
    public Cell getCell(int row, int col) {
        if (!isInBounds(row, col)) {
            return Cell.WALL;
        }
        return grid[row][col];
    }

    /**
     * Retorna la celda en la Position dada.
     */
    public Cell getCell(Position pos) {
        return getCell(pos.getRow(), pos.getCol());
    }

    /**
     * Establece el tipo de celda en las coordenadas dadas.
     */
    public void setCell(int row, int col, Cell cell) {
        if (isInBounds(row, col)) {
            grid[row][col] = cell;
        }
    }

    /**
     * Indica si la posicion es transitable (dentro de limites y no es pared).
     */
    public boolean isWalkable(Position pos) {
        return isInBounds(pos) && getCell(pos).isWalkable();
    }

    /**
     * Indica si las coordenadas estan dentro de los limites del tablero.
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Indica si la posicion esta dentro de los limites del tablero.
     */
    public boolean isInBounds(Position pos) {
        return isInBounds(pos.getRow(), pos.getCol());
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell[][] getGrid() {
        return grid;
    }
}
