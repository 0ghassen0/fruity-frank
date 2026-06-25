package engine.geometry;

/** Marks the part of the world that is actively simulated. */
@FunctionalInterface
public interface ActiveRegion {

	/** Tells whether a world point (cm) is inside the simulated zone. */
	boolean contains(double cmX, double cmY);
}
