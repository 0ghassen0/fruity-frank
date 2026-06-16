package franck.ai;

import engine.entities.Entity;
import engine.gal.aut.Category;
import engine.gal.aut.CategoryMatcher;
import franck.Categorized;

public class FranckCategories implements CategoryMatcher {

    @Override
    public boolean matches(Category cat, Entity self, Entity other) {
        if (!(other instanceof Categorized)) return false;
        franck.Category oc = franck.Category.of(other);
        if (cat == Category.Adversary) return oc == franck.Category.FRANK;
        return false;
    }
}