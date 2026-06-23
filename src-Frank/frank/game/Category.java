package frank.game;

import engine.entities.Entity;

public enum Category {
    FRANK, ENEMY, ENEMY_SPAWNER, GRASS_HIGH, GRASS_LOW,
    APPLE, CHERRY, CHERRY_PROJECTILE, FRUIT, WALL;

    public static Category of(Entity e) {
        if (e instanceof Categorized c) return c.category();
        // Entité non catégorisée (décor, particule…) : pas de catégorie → aucune règle
        // ne s'y applique. On renvoie null plutôt que de lever, sinon une seule entité
        // non catégorisée ferait planter toute la boucle de collision (mono-thread).
        return null;
    }
}