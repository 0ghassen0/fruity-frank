package engine.core;

public enum ZLayer {
    BACKGROUND(0),    // Ciel, montagnes lointaines
    GROUND(1),        // Sol, obstacles, personnages
    REGULAR(2),       // Entités standards
    FLYING(3);        // Oiseaux, avions, projectiles
    
    private final int level;
    ZLayer(int level) { this.level = level; }
    public int getLevel() { return level; }
}