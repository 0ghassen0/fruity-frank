package frank.game;

import java.util.ArrayList;
import java.util.List;

import engine.core.Game;
import engine.core.Model;
import engine.entities.BasicStunt;
import engine.entities.Entity;
import engine.geometry.Grid;
import engine.geometry.ISU;
import frank.entities.*;

public class FruityFrankModel extends Model {

	private final GameState state = new GameState();
	private final Rules rules = new Rules();
	private final FruityFrankMap map;
	private final Game game;
	private Frank player;
	private boolean debugMode = false;
	private int tick = 0;

	private static final double CHERRY_SPEED = 4.0;

	public FruityFrankModel(Game game, FruityFrankMap map) {
		super(game);
		this.game = game;
		this.map = map;
	}

	
	public void setPlayer(Frank p) {
		this.player = p;
	}

	public Frank player() {
		return player;
	}

	public GameState state() {
		return state;
	}

	public FruityFrankMap map() {
		return map;
	}

	public boolean debugMode() {
		return debugMode;
	}

	public void toggleDebug() {
		debugMode = !debugMode;
	}

	// ============================================================
	// UPDATE
	// ============================================================

	@Override
	public void update() {
		
		if (state.isStartMenu())
		    return;
		
	    if (state.isEnded()) {
	        state.tickEndScreenDelay();
	        return;
	    }

	    if (state.isPaused())
	        return;

	    if (state.isCountingDown()) {
	        state.tickCountdown();
	        return;
	    }

	    super.update();
	    tick++;
	    digGrass();
	    updateGrassFade();
	    pushApples();
	    collectUnderFrank();
	    removeBadCherryProjectiles();
	}

	// ============================================================
	// CREUSAGE DU GAZON
	// ============================================================

	/**
	 * Frank horizontal -> détruit seulement le gros gazon (9 cm). Frank vertical ->
	 * détruit le gros ET le petit gazon (1 cm). Ennemis CREUSEURS (canDig) ->
	 * détruisent le gros gazon. Ennemis non creuseurs -> ne détruisent rien (ils
	 * restent dans les tunnels).
	 */
	private boolean centeredEnough(Entity e) {
		ISU.Coord here = e.center();
		ISU.Coord cc = e.position().toISUCoordCentered();
		ISU.Vector delta = here.nearestDeltaTo(cc);

		ISU.Vector v = e.getLinearSpeed();
		double vx = v == null ? 0 : Math.abs(v.x());
		double vy = v == null ? 0 : Math.abs(v.y());
		double cm = here.getISU().getCmPerCell();

		// sur l'axe de déplacement : demi-pas ; sur l'axe perpendiculaire : tolérance
		// fixe large
		double tolX = Math.max(vx / 2.0, 0.45 * cm) + 1e-6;
		double tolY = Math.max(vy / 2.0, 0.45 * cm) + 1e-6;

		return Math.abs(delta.x()) <= tolX && Math.abs(delta.y()) <= tolY;
	}

	private void digGrass() {
		if (player != null && entities().contains(player) && centeredEnough(player))
			dig(player);

		for (Entity e : new ArrayList<>(entities())) {
			if (!(e instanceof Enemy en) || !en.canDig())
				continue;
			if (!centeredEnough(en))
				continue; // <-- même garde
			dig(en);
		}
	}

	/**
	 * Creuse le gazon traversé par {@code digger}.
	 * <ul>
	 * <li>Le gros bloc (GrassHigh) de la cellule courante passe en fondu.</li>
	 * <li>Le petit bloc (GrassLow) n'est que le sol au bas de chaque cellule ; on
	 * ne le détruit que si le CORPS du digger le recouvre réellement. Ainsi un
	 * déplacement horizontal (corps centré, au-dessus du sol) le laisse intact ;
	 * une descente casse le sol de la cellule courante ; une montée casse le sol de
	 * la cellule du dessus dès que Frank l'atteint — sans toucher au sol de la
	 * cellule du dessous au démarrage.</li>
	 * </ul>
	 * Fonder la destruction du sol sur le recouvrement réel (et non sur la cellule
	 * du centre) évite que Frank perce le « petit rect du dessous » ou traverse le
	 * petit rect au-dessus sans le casser.
	 */
	private void dig(Entity digger) {
		for (Entity e : new ArrayList<>(grid().cellAt(digger.position()).entities()))
			if (e instanceof GrassHigh gh)
				gh.startFade();

		for (Grid.Cell cell : new ArrayList<>(digger.getHitbox().occupiedCells()))
			for (Entity e : new ArrayList<>(cell.entities()))
				if (e instanceof GrassLow && digger.intersects(e))
					remove(e);
	}

	private void updateGrassFade() {
		List<Entity> done = new ArrayList<>();
		for (Entity e : new ArrayList<>(entities())) {
			if (e instanceof GrassHigh gh && gh.isFading()) {
				gh.tickFade(); // décrémente le timer (7 → 6 → 5 ...)
				if (gh.fadeDone())
					done.add(gh); // arrivé à 0 → à retirer
			}
		}
		for (Entity e : done)
			remove(e); // suppression effective
	}

	// ============================================================
	// PUSH DES POMMES (glissement continu, basé sur positions réelles)
	// ============================================================

	private void pushApples() {
		if (player == null || !entities().contains(player))
			return;

		double cell = game.getCmPerCell();
		ISU.Vector v = player.getLinearSpeed();
		boolean horizontal = v != null && v.x() != 0 && v.y() == 0;
		int dir = horizontal ? (int) Math.signum(v.x()) : 0;
		boolean wantsH = horizontal;
		Grid grid = grid();
		Grid.Position fp = player.position(); // invariant pendant la passe

		for (Entity e : new ArrayList<>(entities())) {
			if (!(e instanceof Apple apple))
				continue;
			if (apple.isFalling())
				continue;
			var aStunt = apple.stunt();
			if (aStunt != null)
				aStunt.drive(0, 0);

			if (!wantsH)
				continue;

			Grid.Position ap = apple.position();

			// même ligne obligatoire
			if (fp.y() != ap.y())
				continue;

			// distance réelle (cm) entre Frank et la pomme sur X (torique)
			double deltaX = player.center().nearestDeltaTo(apple.center()).x();

			// la pomme doit être DEVANT Frank, et au CONTACT (somme des rayons)
			double contact = 0.35 * cell + 0.40 * cell; // rayon Frank + rayon pomme
			boolean appleAhead = Math.signum(deltaX) == dir;
			boolean touching = Math.abs(deltaX) <= contact + 0.05 * cell;
			if (!appleAhead || !touching)
				continue;

			// La case devant la pomme doit être libre
			Grid.Position beyond = grid.new Position(ap.x() + dir, ap.y());
			if (!canReceiveApple(beyond))
				continue;

			// La pomme écrase le gazon de la case où elle ARRIVE, puis s'y installe
			for (Entity o : new ArrayList<>(grid.cellAt(beyond).entities())) {
				if (o instanceof GrassHigh gh)
					gh.startFade();
				if (o instanceof GrassLow gl)
					remove(gl);
			}

			// Déplace la pomme proprement d'une case (alignée sur la grille)
			apple.setPosition(beyond);
		}
	}
	// ============================================================
	// RAMASSAGE
	// ============================================================

	private void collectUnderFrank() {
	    if (player == null || !entities().contains(player))
	        return;

	    Grid.Cell cell = grid().cellAt(player.position());
	    List<Entity> got = new ArrayList<>();

	    for (Entity e : cell.entities()) {
	        if (e instanceof Fruit || e instanceof Cherry)
	            got.add(e);
	    }

	    for (Entity e : got) {
	        remove(e);

	        if (e instanceof Fruit f) {
	            state.addScore(pointsForFruit(f));
	            state.collectFruit();
	        }

	        if (e instanceof Cherry) {
	            player.pickCherry();
	            state.addScore(50);
	            state.collectFruit();
	        }
	    }
	}

	// ============================================================
	// CHUTE DES POMMES
	// ------------------------------------------------------------
	// La chute n'est plus un passage impératif ici : chaque pomme porte un
	// GALBot exécutant gal_fsm/AppleFall.gal ( Struck ? Move(S) ). La garde
	// « Struck » est la condition Java frank.gal.aut.conditions.CanFall, qui
	// décide QUAND la pomme peut tomber (support en dessous + petit délai).
	// L'écrasement du gazon fin et les morts par chute sont résolus comme des
	// collisions (voir Rules : APPLE×GRASS_LOW / APPLE×FRANK / APPLE×ENEMY) et
	// l'arrêt sur un support solide est garanti par canOccupy ci-dessous.
	// ============================================================

	// ============================================================
	// PROJECTILES CERISE
	// ============================================================

	private void removeBadCherryProjectiles() {
		Grid grid = grid();
		List<Entity> dead = new ArrayList<>();
		for (Entity e : entities()) {
			if (!(e instanceof CherryProjectile cp))
				continue;
			 if (cp.outOfRange()) {
			        dead.add(cp);
			        continue;
			    }
			// La case juste devant le projectile, dans sa direction de tir
			Grid.Position cpos = cp.position();
			Grid.Position next = grid.new Position(cpos.x() + cp.facingDx(), cpos.y() + cp.facingDy());

			// Mur → la cerise disparaît
			if (map.isWall(next.x(), next.y())) {
				dead.add(cp);
				continue;
			}

			// Tout obstacle dans la case suivante → la cerise disparaît
			for (Entity o : grid.cellAt(next).entities()) {
				if (o instanceof GrassHigh || o instanceof Fruit || o instanceof Apple
						|| o instanceof Cherry) {
					dead.add(cp);
					break;
				}
			}
		}
		for (Entity e : dead)
			remove(e);
	}

	public void throwCherryInternal(Frank frank) {
	    int fdx = frank.facingDx();
	    int fdy = frank.facingDy();
	    int sx = frank.position().x() + fdx;
	    int sy = frank.position().y() + fdy;

	    Grid.Position p = grid().new Position(sx, sy);

	    // Si Frank tire dans un mur, il garde sa cerise.
	    if (map.isWall(p.x(), p.y()))
	        return;

	    CherryProjectile proj = new CherryProjectile(game, sx, sy, fdx, fdy);
	    proj.setStunt(new BasicStunt(proj));
	    proj.stunt().drive(fdx * CHERRY_SPEED, fdy * CHERRY_SPEED);

	    add(proj);

	    // Là seulement, le projectile a vraiment été lancé.
	    frank.useCherry();
	}
	// ============================================================
	// PHYSIQUE
	// ============================================================

	@Override
	public boolean canOccupy(Entity e, Grid.Position target) {

		if (map.isWall(target.x(), target.y()))
			return false;

		Grid grid = grid();

		if (e instanceof Apple falling) {
			// Une pomme qui tombe est arrêtée par un support solide (gros gazon,
			// autre pomme, fruit, cerise). Sinon elle traverse la case : le gazon
			// fin, Frank et les ennemis y sont écrasés/tués via les règles de
			// collision (voir Rules), pas ici.
			if (cellHas(target, GrassHigh.class) || cellHas(target, Fruit.class) || cellHas(target, Cherry.class))
				return false;
			for (Entity o : grid.cellAt(target).entities())
				if (o instanceof Apple a && a != falling)
					return false;
			return true;
		}

		if (e instanceof Frank frank) {
			boolean hasApple = false;
			for (Entity o : grid.cellAt(target).entities())
				if (o instanceof Apple) {
					hasApple = true;
					break;
				}

			if (hasApple) {
				ISU.Vector toTarget = frank.center().nearestDeltaTo(target.toISUCoordCentered());
				int dx = (int) Math.signum(toTarget.x());
				int dy = (int) Math.signum(toTarget.y());
				if (dy != 0 || dx == 0)
					return false; // pas de poussée verticale
				Grid.Position beyond = grid.new Position(target.x() + dx, target.y());
				return canReceiveApple(beyond);
			}
		}

		if (e instanceof Enemy en) {
			// une pomme posée (stable) est un obstacle pour TOUS les monstres
			Grid.Cell cell = grid.cellAt(target);
			for (Entity o : cell.entities())
				if ((o instanceof Apple a && !a.isFalling()) || (o instanceof Enemy && o != e))
					return false;

			if (en.canDig())
				return true;
			for (Entity o : cell.entities())
				if (o instanceof GrassHigh)
					return false;
			return true;
		}

		if (e instanceof CherryProjectile) {
			for (Entity o : grid.cellAt(target).entities())
				if (o instanceof GrassHigh)
					return false;
			return true;
		}

		return true;
	}

	private boolean canReceiveApple(Grid.Position pos) {
		if (map.isWall(pos.x(), pos.y()))
			return false;
		for (Entity e : grid().cellAt(pos).entities()) {
			if (e instanceof Apple)
				return false;
			if (e instanceof Fruit)
				return false;
			if (e instanceof Cherry)
				return false;
			if (e instanceof Enemy)
				return false;
		}
		return true;
	}

	// ============================================================
	// DIVERS
	// ============================================================

	@Override
	protected void onCollision(Entity a, Entity b) {
		rules.resolve(this, state, a, b);
	}

	private boolean cellHas(Grid.Position pos, Class<?> type) {
		for (Entity e : grid().cellAt(pos).entities())
			if (type.isInstance(e))
				return true;
		return false;
	}

	private int pointsForFruit(Fruit f) {
		return switch (f.type()) {
		case BANANA -> 150;
		case ORANGE -> 180;
		case STRAWBERRY -> 250;
		case PLUM -> 200;
		case GRAPES -> 120;
		};
	}

	public int enemyCount() {
	    int count = 0;

	    for (Entity e : entities()) {
	        if (e instanceof Enemy)
	            count++;
	    }

	    return count;
	}
}