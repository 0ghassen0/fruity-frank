package game.graphics;

import java.util.EnumMap;

import game.Category;

// == PALETTE ==

/**
 * The Pac-Man colour scheme, keyed by {@link Category}. Colours are kept as {r,
 * g, b} triples (not {@code oop.graphics.Color}) because a {@code Color} can
 * only be created from a live {@code Graphics} at paint time; the avatar turns
 * a triple into a {@code Color} when it draws.
 */
public final class Palette {

	private static final int[] DEFAULT = { 200, 200, 200 };
	private static final EnumMap<Category, int[]> RGB = new EnumMap<>(Category.class);

	static {
		RGB.put(Category.PAC, new int[] { 255, 255, 0 }); // yellow
		RGB.put(Category.GHOST, new int[] { 255, 0, 0 }); // red
		RGB.put(Category.GUM, new int[] { 255, 192, 203 }); // pink
		RGB.put(Category.BOSS, new int[] { 255, 0, 255 }); // magenta
	}

	private Palette() {
	}

	/** @return the {r, g, b} colour for {@code category}, or a grey fallback. */
	public static int[] rgb(Category category) {
		return RGB.getOrDefault(category, DEFAULT);
	}
}
