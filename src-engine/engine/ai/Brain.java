package engine.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import engine.core.Model;
import engine.core.Ticker;
import engine.entities.Bot;
import engine.entities.Entity;

// == BRAIN ==

/** AI scheduler: each tick wakes every active bot so it can re-decide. */
public class Brain implements Ticker.TickListener {

	/** Bottom-to-top order (largest grid {@code y} first) so gravity settles a column cleanly. */
	public static final Comparator<Entity> BOTTOM_FIRST = Comparator.comparingInt((Entity e) -> -e.position().y())
			.thenComparingInt(e -> e.position().x());

	private final Model model;

	/** Optional order in which bots are woken; null = unspecified. */
	private Comparator<Entity> activationOrder;

	public Brain(Model model) {
		this.model = model;
	}

	/** Sets the order bots are woken each tick. */
	public void setActivationOrder(Comparator<Entity> order) {
		this.activationOrder = order;
	}

	/** Wakes each active entity's bot once for this AI tick. */
	@Override
	public void onTick(double deltaTime) {
		List<Entity> snapshot = new ArrayList<>();

		for (Entity e : model.entities()) {
			if (model.isActiveForSimulation(e))
				snapshot.add(e);
		}

		if (activationOrder != null)
			snapshot.sort(activationOrder);

		for (Entity e : snapshot) {
			Bot bot = e.bot();
			if (bot != null)
				bot.tick(deltaTime);
		}
	}
}
