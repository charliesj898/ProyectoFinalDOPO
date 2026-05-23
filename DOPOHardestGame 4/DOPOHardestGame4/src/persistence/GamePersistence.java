package persistence;

import domain.Game;

/**
 * Interfaz para definir el contrato de persistencia del estado del juego.
 * Permite guardar y cargar una partida desde/hacia un archivo.
 */
public interface GamePersistence {

    /**
     * Guarda el estado actual del juego.
     * 
     * @param game     La instancia del juego a guardar.
     * @param filename El nombre o ruta del archivo de guardado.
     * @throws Exception Si ocurre un error durante el guardado.
     */
    void save(Game game, String filename) throws Exception;

    /**
     * Carga el estado de un juego previamente guardado.
     * 
     * @param filename El nombre o ruta del archivo de guardado.
     * @return La instancia del juego cargada.
     * @throws Exception Si ocurre un error al leer o deserializar.
     */
    Game load(String filename) throws Exception;
}
