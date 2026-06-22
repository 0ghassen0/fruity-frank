package engine.gal.conditions;

import engine.core.Model;
import engine.entities.Entity;
import engine.geometry.ISU;
import engine.gal.aut.Category;
import engine.gal.aut.CategoryMatcher;
import engine.gal.aut.Direction;
import engine.gal.aut.GALBot;
import engine.gal.aut.iGALCondition;

// == CLOSEST ==

/**
 * The GAL {@code Closest(Category[, Direction][, range])} condition. It always
 * <i>selects</i> the nearest matching entity (torus-aware distance) into the
 * bot's {@code $} variable, then returns a boolean:
 *
 * <ul>
 * <li>{@code Closest(cat)} — true iff such an entity exists.</li>
 * <li>{@code Closest(cat, dir)} — true iff the nearest one lies in the
 * {@code dir} quadrant (a 90° cadran). Note the selection is the nearest
 * <i>overall</i>; the direction only gates the boolean — this is the
 * interpretation the spec's flee example relies on.</li>
 * <li>a {@code range} (in steps/cells) limits the candidates to those within
 * that radius; beyond it the condition is inoperative (false,
 * {@code $ = null}).</li>
 * </ul>
 *
 * <p>
 * Naive O(n) scan per evaluation. The spec's region/cache optimisation (hosted
 * in the {@code Brain}, refreshed on movement) is a later performance
 * refinement; the behaviour here is the reference semantics it must match.
 */
public final class Closest implements iGALCondition {

	private final Category cat;
	private final Direction dir; // null = any direction (boolean = "exists")
	private final int rangeSteps; // <= 0 = unlimited

	public Closest(Category cat) {
		this(cat, null, 0);
	}

	public Closest(Category cat, Direction dir) {
		this(cat, dir, 0);
	}

	public Closest(Category cat, int rangeSteps) {
		this(cat, null, rangeSteps);
	}

	public Closest(Category cat, Direction dir, int rangeSteps) {
		this.cat = cat;
		this.dir = dir;
		this.rangeSteps = rangeSteps;
	}

	@Override
	public boolean eval(Entity self) {
		if (!(self.bot() instanceof GALBot bot))
			return false;
		CategoryMatcher matcher = bot.matcher();
		Model model = self.model();
		if (matcher == null || model == null)
			return false;

		// One read of our centre (snapshot): this runs on the AI thread while the
		// motion thread may swap the entity's centre reference, so every distance in
		// the scan is measured from the same origin.
		ISU.Coord selfCenter = self.center();
		double maxDist = rangeSteps > 0 ? rangeSteps * selfCenter.getISU().getCmPerCell() : Double.MAX_VALUE;
		Entity nearest = null;
		ISU.Vector nearestDelta = null;
		double best = Double.MAX_VALUE;
		for (Entity other : model.entities()) {
			if (other == self || !matcher.matches(cat, self, other))
				continue;
			ISU.Vector delta = selfCenter.nearestDeltaTo(other.center());
			double d = delta.norm();
			if (d <= maxDist && d < best) {
				best = d;
				nearest = other;
				nearestDelta = delta;
			}
		}

		bot.select(nearest); // $ = nearest (or null)
		if (nearest == null)
			return false;
		if (dir == null)
			return true;
		return Direction.cardinal(nearestDelta.x(), nearestDelta.y()) == dir;
	}
}
