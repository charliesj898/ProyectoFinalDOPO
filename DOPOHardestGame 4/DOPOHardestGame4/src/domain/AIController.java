package domain;

/**
 * Interfaz abstracta para controladores de inteligencia artificial.
 * Cada implementacion define una estrategia diferente de movimiento.
 * 
 * Para agregar un nuevo perfil de IA:
 * 1. Crear una nueva clase que extienda AIController.
 * 2. Agregar el perfil correspondiente a AIProfile.
 * 3. Instanciarlo en AIControllerFactory.
 */
import java.io.Serializable;

public abstract class AIController implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Calcula la siguiente direccion de movimiento para el jugador IA.
     * @param player el jugador controlado por la IA
     * @param level  el nivel actual
     * @return la direccion de movimiento, o null si debe quedarse quieto
     */
    public abstract Direction getNextDirection(Player player, Level level);

    /**
     * Fabrica que crea el controlador de IA segun el perfil.
     * @param profile perfil de IA deseado
     * @return instancia del AIController correspondiente
     */
    public static AIController create(AIProfile profile) {
        switch (profile) {
            case RANDOM: return new RandomAIController();
            case EXPERT: return new ExpertAIController();
            default:     return new ExpertAIController();
        }
    }
}
