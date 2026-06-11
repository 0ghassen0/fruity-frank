package engine.gal.arguments;

import java.util.HashMap;
import java.util.Map;

public class Category {

    private static final Map<String, Category> categories = new HashMap<>();
    private static final int MAX = 20;
    private static final boolean[][] interaction = new boolean[MAX][MAX];
    private static int nextIndex = 0;

    public static final Category Adversary = new Category("A", nextIndex++);
    public static final Category Obstacle  = new Category("O", nextIndex++);
    public static final Category Team      = new Category("T", nextIndex++);
    public static final Category Void      = new Category("V", nextIndex++);
    public static final Category Player    = new Category("@", nextIndex++);
    public static final Category Gum       = new Category("U", nextIndex++);
    public static final Category Clue      = new Category("C", nextIndex++);
    public static final Category Any       = new Category("_", nextIndex++);

    private final String name;
    private final int index;

    private Category(String name, int index) {
        assert name != null;
        this.name = name;
        this.index = index;
        categories.put(name, this);
    }

    public static Category canonical(String name) {
        assert name != null;
        Category c = categories.get(name);
        assert c != null : "Unknown category: " + name;
        return c;
    }

    public String name() { return name; }
    public int index()   { return index; }

    public static void setInteraction(Category c1, Category c2, boolean bool) {
        assert c1 != null && c2 != null;
        interaction[c1.index][c2.index] = bool;
        interaction[c2.index][c1.index] = bool;
    }

    public boolean interactsWith(Category c) {
        assert c != null;
        if (this == Any || c == Any) return true;
        return interaction[this.index][c.index];
    }

    @Override
    public String toString() { return name; }
}