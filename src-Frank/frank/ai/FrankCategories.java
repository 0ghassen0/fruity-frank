package frank.ai;

import engine.entities.Entity;
import engine.gal.aut.Category;
import engine.gal.aut.CategoryMatcher;
import frank.game.Categorized;

public class FrankCategories implements CategoryMatcher {

    @Override
    public boolean matches(Category cat, Entity self, Entity other) {
        if (!(other instanceof Categorized)) return false;
        frank.game.Category oc = frank.game.Category.of(other);
        if (cat == Category.Adversary) return oc == frank.game.Category.FRANK;
        return false;
    }
}