package domain;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Clase que se usa para el registro de eventos y errores del juego.
 * Escribe en un archivo de log para el seguimiento de errores.
 */
public class GameLogger {

    private static final Logger LOGGER = Logger.getLogger("DOPOHardestGame");
    private static boolean initialized = false;

    /**
     * Inicializa el logger con un manejador de archivo.
     * Se debe llamar una vez al iniciar la aplicacion.
     */
    public static void init() {
        if (initialized) {
            return;
        }
        try {
            FileHandler fileHandler = new FileHandler("dopo_game.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
            initialized = true;
            info("GameLogger inicializado correctamente");
        } catch (IOException e) {
            System.err.println("Error al inicializar el logger: " + e.getMessage());
        }
    }

    /**
     * Registra un mensaje informativo.
     */
    public static void info(String message) {
        LOGGER.info(message);
    }

    /**
     * Registra una advertencia.
     */
    public static void warning(String message) {
        LOGGER.warning(message);
    }

    /**
     * Registra un error.
     */
    public static void error(String message) {
        LOGGER.severe(message);
    }

    /**
     * Registra un error con la excepcion asociada.
     */
    public static void error(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }
}
