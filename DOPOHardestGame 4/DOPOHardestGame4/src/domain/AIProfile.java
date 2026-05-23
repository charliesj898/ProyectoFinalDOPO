package domain;

/**
 * Perfil de inteligencia artificial para el modo Player vs Machine.
 */
public enum AIProfile {

    /** Mueve en una direccion valida aleatoria en cada tick. */
    RANDOM("Aleatoria"),

    /** Usa la mejor estrategia disponible para ganar. */
    EXPERT("Experta");

    private final String displayName;

    AIProfile(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
