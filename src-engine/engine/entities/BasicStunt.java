package engine.entities;

import engine.core.Model;
import engine.geometry.ISU;

// == BASIC STUNT ==

/**
 * A simple stunt that actuates the bot's pushed command: each tick it rotates
 * by the commanded angular velocity and translates by the commanded linear
 * velocity, provided the destination cell is legal ({@link Model#canOccupy}).
 *
 * <p>
 * It also implements the {@code Stunt} deferred-command seam for grid turn
 * buffering: a perpendicular turn requested mid-cell ({@link #request}) is held
 * in a single slot and applied at the next crossing, snapping the entity onto
 * the new lane's centre line. A direct {@link #drive} cancels any pending turn.
 *
 * <p>
 * Game-agnostic engine plumbing: the only game knowledge it consults is the
 * {@link Model#canOccupy} predicate (what counts as a wall), so it is reusable
 * by any grid-movement game without subclassing.
 */
public class BasicStunt extends Stunt {

	// Single-slot buffered turn (only the most recent request is kept).
	private double reqVx, reqVy;
	private boolean hasReq;

	public BasicStunt(Entity owner) {
		super(owner);
	}

	// DEFERRED COMMAND (turn buffering)

	@Override
	public void request(double vx, double vy) {
		reqVx = vx;
		reqVy = vy;
		hasReq = true;
	}

	@Override
	public void clearRequest() {
		hasReq = false;
	}

	@Override
	public void drive(double vx, double vy) {
		clearRequest(); // a direct command supersedes any buffered turn
		super.drive(vx, vy);
	}

	/**
	 * Set when a micro-step hit a wall this tick, latching the rest of the tick.
	 */
	private boolean blocked;

	@Override
	public void beginTick(Model model) {
		super.beginTick(model); // count down any timed-action busy timer
		blocked = false;
		// Continuous rotation first, so translation uses the updated orientation.
		double omega = owner.getAngularSpeed_degree();
		if (omega != 0)
			owner.turn((int) Math.round(omega));

		// A buffered turn (player pressed perpendicular mid-cell) takes effect once
		// the entity reaches a crossing — then it snaps onto the new lane's centre.
		applyBufferedTurnAtCrossing(model);
	}

	@Override
	public void advance(Model model, int i, int n) {
		if (blocked) // already wedged against a wall this tick
			return;

		ISU.Vector speed = owner.getLinearSpeed();
		if (speed == null || (speed.x() == 0 && speed.y() == 0))
			return;

		// This micro = the i-th of n equal slices of the full step, with the last
		// slice computed as the remainder so the n slices sum to the step exactly
		// (n == 1 reproduces a single full translate bit-for-bit).
		ISU isu = owner.center().getISU();
		double mx = (i < n - 1) ? speed.x() / n : speed.x() - speed.x() / n * (n - 1);
		double my = (i < n - 1) ? speed.y() / n : speed.y() - speed.y() / n * (n - 1);
		if (mx == 0 && my == 0)
			return;
		ISU.Vector micro = isu.new Vector(mx, my);

		// Consult the game's rule before committing the micro (walls, blocked cells).
		// The probe is the body's *leading edge* (half a cell ahead along the travel
		// axis), not the centre: checking only the centre lets the body slide half-way
		// into a wall before the centre crosses, leaving it wedged and unable to turn.
		// Capping each micro at half a cell guarantees this probe never skips a wall.
		// When the edge would enter a wall, stop and snap flush onto the current lane
		// cell's centre so the entity rests against the wall and can turn aside; the
		// command is kept (not zeroed) so the entity keeps pushing / can turn next
		// tick.
		if (stepHitsWall(model, micro)) {
			snapToLaneCellAlong(micro);
			blocked = true;
			return;
		}

		owner.translate(micro);
	}

	// WALL CLEARANCE

	/**
	 * @return whether taking {@code step} would push any part of the body into a
	 *         wall. Probes the leading edge — the centre plus half the cell along
	 *         the travel axis — nudged inward by an epsilon so an edge that merely
	 *         <i>touches</i> the wall boundary (the lane-aligned rest position)
	 *         stays legal. The perpendicular axis keeps the centre, since a
	 *         lane-centred entity never spans more than its own row/column there.
	 */
	private boolean stepHitsWall(Model model, ISU.Vector step) {
		ISU.Coord c = owner.center();
		ISU isu = c.getISU();
		double reach = isu.getCmPerCell() / 2.0 - 1e-6;
		double sx = Math.signum(step.x());
		double sy = Math.signum(step.y());
		double px = c.x() + step.x() + sx * reach;
		double py = c.y() + step.y() + sy * reach;
		ISU.Coord probe = isu.new Coord(px, py);
		return !model.canOccupy(owner, probe.toGridPosition());
	}

	/**
	 * Snap the travel-axis coordinate onto the current lane cell's centre, leaving
	 * the perpendicular axis untouched, so the body rests flush against the wall.
	 */
	private void snapToLaneCellAlong(ISU.Vector step) {
		ISU.Coord here = owner.center();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		double nx = step.x() != 0 ? cellCenter.x() : here.x();
		double ny = step.y() != 0 ? cellCenter.y() : here.y();
		owner.setCoord(here.getISU().new Coord(nx, ny));
	}

	// BUFFERED TURN

	/**
	 * If a perpendicular turn is buffered, apply it once the entity is aligned on a
	 * crossing — i.e. its position along the <i>current</i> travel axis is at (or
	 * within half a step of) a cell centre. A stopped entity turns immediately.
	 * When applied, the travel-axis coordinate is snapped exactly onto the cell
	 * centre so the entity sits on the new lane's centre line, and the buffered
	 * command becomes the active one.
	 *
	 * <p>
	 * The turn is only taken if the target cell is legal ({@link Model#canOccupy}):
	 * a buffered turn into a wall is held, not spent, so the entity keeps going and
	 * turns at the next crossing where that direction opens up.
	 */
	private void applyBufferedTurnAtCrossing(Model model) {
		if (!hasReq)
			return;

		ISU.Vector active = owner.getLinearSpeed();
		boolean stopped = active == null || (active.x() == 0 && active.y() == 0);
		if (stopped) {
			if (turnIsLegal(model)) // not moving: turn now if the way is clear
				snapAndApply(active);
			return;
		}

		boolean travellingX = active.x() != 0;
		ISU.Coord here = owner.center();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		double offset = travellingX ? here.x() - cellCenter.x() : here.y() - cellCenter.y();
		double halfStep = Math.abs(travellingX ? active.x() : active.y()) / 2.0 + 1e-9;

		if (Math.abs(offset) <= halfStep && turnIsLegal(model))
			snapAndApply(active);
	}

	/**
	 * @return whether the cell one step in the buffered direction can be entered.
	 */
	private boolean turnIsLegal(Model model) {
		double cmPerCell = owner.center().getISU().getCmPerCell();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		ISU.Vector step = cellCenter.getISU().new Vector(Math.signum(reqVx) * cmPerCell,
				Math.signum(reqVy) * cmPerCell);
		return model.canOccupy(owner, cellCenter.mkTranslated(step).toGridPosition());
	}

	/**
	 * Snap the travel-axis coordinate to the cell centre, then activate the turn.
	 */
	private void snapAndApply(ISU.Vector active) {
		ISU.Coord here = owner.center();
		ISU.Coord cellCenter = owner.position().toISUCoordCentered();
		double nx = here.x();
		double ny = here.y();
		if (active != null && active.x() != 0)
			nx = cellCenter.x(); // travelling east/west -> snap x onto the new vertical lane
		else if (active != null && active.y() != 0)
			ny = cellCenter.y(); // travelling north/south -> snap y onto the new horizontal lane
		owner.setCoord(here.getISU().new Coord(nx, ny));

		double vx = reqVx;
		double vy = reqVy;
		drive(vx, vy); // clears the buffer and sets the new active command
	}
}
