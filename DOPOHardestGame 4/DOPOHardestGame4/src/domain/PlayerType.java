package domain;

import java.awt.Color;

/**
 * Define los diferentes tipos de jugador con sus caracteristicas unicas.
 * 
 * RED:   Velocidad normal (1.0x), tamano normal, sin habilidades especiales.
 * BLUE:  Velocidad rapida (1.5x), tamano menor, sin habilidades especiales.
 * GREEN: Velocidad normal (1.0x), tamano normal, escudo que absorbe un golpe.
 */
public enum PlayerType {

    RED(1.0f, 20, false, new Color(220, 50, 50)),
    BLUE(1.5f, 30, false, new Color(50, 120, 220)),
    GREEN(1.0f, 20, true, new Color(100, 220, 100));

    private final float baseSpeed;
    private final int size;
    private final boolean hasShield;
    private final Color color;

    PlayerType(float baseSpeed, int size, boolean hasShield, Color color) {
        this.baseSpeed = baseSpeed;
        this.size = size;
        this.hasShield = hasShield;
        this.color = color;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    /**
     * Retorna el tamano visual del jugador en pixeles.
     */
    public int getSize() {
        return size;
    }

    /**
     * Indica si este tipo de jugador tiene escudo protector.
     */
    public boolean hasShield() {
        return hasShield;
    }

    /**
     * Retorna el color de representacion visual del jugador.
     */
    public Color getColor() {
        return color;
    }
}
