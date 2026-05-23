package domain;

/**
 * Excepcion personalizada para errores especificos del juego.
 * Utilizada para manejar situaciones como niveles invalidos,
 * configuraciones corruptas, o errores de logica del juego.
 */
public class GameException extends Exception {

    /**
     * Crea una excepcion del juego con un mensaje descriptivo.
     * @param message mensaje de error
     */
    public GameException(String message) {
        super(message);
    }

    /**
     * Crea una excepcion del juego con un mensaje y una causa.
     * @param message mensaje de error
     * @param cause excepcion original que causo este error
     */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
