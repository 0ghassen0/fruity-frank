package frank.game;

import java.util.EnumMap;
import engine.entities.Entity;
import frank.entities.Apple;
import frank.entities.Frank;

public class Rules {

	@FunctionalInterface
	public interface Outcome {
		void apply(FruityFrankModel m, GameState s, Entity self, Entity other);
	}

	private final EnumMap<Category, EnumMap<Category, Outcome>> table = new EnumMap<>(Category.class);

	public Rules() {
		// Frank touche un ennemi → perd une vie
		on(Category.FRANK, Category.ENEMY, (m, s, frank, enemy) -> {
//			s.killedByEnemy();
//			m.remove(frank);
		});

		// Cerise projectile touche un ennemi → ennemi meurt
		on(Category.CHERRY_PROJECTILE, Category.ENEMY, (m, s, proj, enemy) -> {
			m.remove(enemy);
			m.remove(proj);
			s.addScore(300);
		});

		// Pomme EN CHUTE qui rattrape Frank → Frank perd une vie
		on(Category.APPLE, Category.FRANK, (m, s, apple, frank) -> {
			if (((Apple) apple).isFalling()) {
				s.killedByEnemy();
				m.remove(frank);
			}
		});

		// Pomme EN CHUTE qui écrase un ennemi → ennemi meurt
		on(Category.APPLE, Category.ENEMY, (m, s, apple, enemy) -> {
			if (((Apple) apple).isFalling()) {
				m.remove(enemy);
				s.addScore(500);
			}
		});

		// Pomme EN CHUTE qui touche le gazon fin → marque une courte pause posée dessus
		// (contact = la pomme est pile à la bonne hauteur). Le décompte et l'écrasement
		// sont gérés par la garde CanFall ; la règle ne fait qu'armer et stopper.
		on(Category.APPLE, Category.GRASS_LOW, (m, s, apple, grass) -> {
			Apple a = (Apple) apple;
			if (a.isFalling() && !a.isLowArmed()) {
				a.armLow(Apple.LOW_PAUSE_TICKS);
				if (a.stunt() != null)
					a.stunt().drive(0, 0); // s'arrête posée sur le gazon fin
			}
		});
	}

	public void resolve(FruityFrankModel model, GameState state, Entity a, Entity b) {
		if (apply(model, state, a, b))
			return;
		apply(model, state, b, a);
	}

	private void on(Category self, Category other, Outcome o) {
		table.computeIfAbsent(self, k -> new EnumMap<>(Category.class)).put(other, o);
	}

	private boolean apply(FruityFrankModel m, GameState s, Entity self, Entity other) {
		var row = table.get(Category.of(self));
		if (row == null)
			return false;
		var o = row.get(Category.of(other));
		if (o == null)
			return false;
		o.apply(m, s, self, other);
		return true;
	}
}