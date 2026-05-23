package domain;

/**
 * Temporizador del juego que maneja el tiempo restante por nivel.
 * Cada nivel tiene un limite de 3 minutos (180 segundos).
 * 
 * Usa System.currentTimeMillis() internamente para medir el tiempo transcurrido.
 * Soporta pausar y reanudar sin perder precision.
 */
import java.io.Serializable;

public class GameTimer implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Tiempo limite por nivel en segundos (3 minutos) */
    public static final int DEFAULT_TIME_LIMIT = 180;

    private int timeLimitSeconds;
    private long startTimeMillis;
    private long pausedElapsedMillis;
    private boolean running;
    private boolean paused;

    public GameTimer() {
        this(DEFAULT_TIME_LIMIT);
    }

    public GameTimer(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
        this.running = false;
        this.paused = false;
        this.pausedElapsedMillis = 0;
    }

    /**
     * Cambia el limite de tiempo. Debe llamarse antes de start().
     */
    public void setTimeLimit(int seconds) {
        this.timeLimitSeconds = seconds;
    }

    /**
     * Inicia o reinicia el temporizador.
     */
    public void start() {
        this.startTimeMillis = System.currentTimeMillis();
        this.pausedElapsedMillis = 0;
        this.running = true;
        this.paused = false;
    }

    /**
     * Pausa el temporizador, guardando el tiempo transcurrido.
     */
    public void pause() {
        if (running && !paused) {
            pausedElapsedMillis = System.currentTimeMillis() - startTimeMillis;
            paused = true;
        }
    }

    /**
     * Reanuda el temporizador desde donde se pauso.
     */
    public void resume() {
        if (running && paused) {
            startTimeMillis = System.currentTimeMillis() - pausedElapsedMillis;
            paused = false;
        }
    }

    /**
     * Retorna los milisegundos transcurridos en el nivel actual.
     */
    public long getElapsedMillis() {
        if (!running && !paused) {
            return 0;
        }
        if (paused) {
            return pausedElapsedMillis;
        }
        return System.currentTimeMillis() - startTimeMillis;
    }

    /**
     * Retorna los segundos restantes del nivel.
     * @return segundos restantes (minimo 0)
     */
    public int getRemainingSeconds() {
        if (!running) {
            return timeLimitSeconds;
        }

        long elapsedMillis = getElapsedMillis();

        int elapsedSeconds = (int) (elapsedMillis / 1000);
        int remaining = timeLimitSeconds - elapsedSeconds;
        return Math.max(0, remaining);
    }

    /**
     * Indica si el tiempo se ha agotado.
     */
    public boolean isExpired() {
        return running && getRemainingSeconds() <= 0;
    }

    /**
     * Indica si el temporizador esta corriendo.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Indica si el temporizador esta pausado.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Detiene el temporizador completamente.
     */
    public void stop() {
        this.running = false;
        this.paused = false;
    }

    /**
     * Restaura el estado del temporizador.
     */
    public void restoreState(int timeLimitSeconds, long pausedElapsedMillis, boolean running, boolean paused) {
        this.timeLimitSeconds = timeLimitSeconds;
        this.pausedElapsedMillis = pausedElapsedMillis;
        this.running = running;
        this.paused = paused;
        if (running && !paused) {
            this.startTimeMillis = System.currentTimeMillis() - pausedElapsedMillis;
        }
    }

    /**
     * Retorna el tiempo restante formateado como "MM:SS".
     */
    public String getFormattedTime() {
        int remaining = getRemainingSeconds();
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
