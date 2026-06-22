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
	private static final int FALL_DELAY_TICKS = 30; // ~1 s à 30 TPS
	private static final int FALL_STEP_TICKS = 3; // vitesse de chute : 1 case / 3 ticks
	private static final int LOW_DELAY_TICKS = 15; // pause sur une ligne fine avant écrasement

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
	    if (state.isEnded()) {
	        state.tickEndScreenDelay();
	        return;
	    }

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
	    updateAppleFall();
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
	    ISU.Coord cc   = e.position().toISUCoordCentered();
	    ISU.Vector delta = here.nearestDeltaTo(cc);

	    ISU.Vector v = e.getLinearSpeed();
	    double vx = v == null ? 0 : Math.abs(v.x());
	    double vy = v == null ? 0 : Math.abs(v.y());
	    double cm = here.getISU().getCmPerCell();

	    // sur l'axe de déplacement : demi-pas ; sur l'axe perpendiculaire : tolérance fixe large
	    double tolX = Math.max(vx / 2.0, 0.45 * cm) + 1e-6;
	    double tolY = Math.max(vy / 2.0, 0.45 * cm) + 1e-6;

	    return Math.abs(delta.x()) <= tolX && Math.abs(delta.y()) <= tolY;
	}
	private void digGrass() {
	    if (player != null && entities().contains(player) && centeredEnough(player)) {
	        ISU.Vector v = player.getLinearSpeed();
	        boolean vertical = v != null && v.y() != 0 && v.x() == 0;
	        digCell(player.position(), true, vertical);
	    }

	    for (Entity e : new ArrayList<>(entities())) {
	    	if (e.name().contains("RED")) {
	    	    ISU.Coord h = e.center();
	    	    var p = e.position();
	    	    ISU.Vector v = e.getLinearSpeed();
	    	    System.out.printf("RED center=(%.1f,%.1f) pos=(%d,%d) speed=(%.2f,%.2f)%n",
	    	        h.x(), h.y(), p.x(), p.y(),
	    	        v==null?0:v.x(), v==null?0:v.y());
	    	}
	        if (!(e instanceof Enemy en) || !en.canDig()) continue;
	        if (!centeredEnough(en)) continue;                  // <-- même garde
	        ISU.Vector ev = en.getLinearSpeed();
	        boolean enemyVertical = ev != null && ev.y() != 0 && ev.x() == 0;
	        digCell(en.position(), true, enemyVertical);
	    }
	}
	private void digCell(Grid.Position pos, boolean digHigh, boolean digLow) {
		 System.out.println("DIG " + pos.x() + "," + pos.y()
         + " high=" + digHigh + " low=" + digLow);
		for (Entity e : new ArrayList<>(grid().cellAt(pos).entities())) {
			if (digHigh && e instanceof GrassHigh gh)
				gh.startFade();
			if (digLow && e instanceof GrassLow)
				remove(e);
		}
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
	    int dir = player.facingDx();
	    boolean wantsH = (dir != 0) && (player.facingDy() == 0);

	    for (Entity e : new ArrayList<>(entities())) {
	        if (!(e instanceof Apple apple))
	            continue;
	        if (apple.isFalling())
	            continue;
	        if (apple.stunt() != null)
	            apple.stunt().drive(0, 0);

	        if (!wantsH)
	            continue;

	        Grid.Position fp = player.position();
	        Grid.Position ap = apple.position();

	        // même ligne obligatoire
	        if (fp.y() != ap.y())
	            continue;

	        // distance réelle (cm) entre Frank et la pomme sur X (torique)
	        double deltaX = player.center().nearestDeltaTo(apple.center()).x();

	        // la pomme doit être DEVANT Frank, et au CONTACT (somme des rayons)
	        double contact = 0.35 * cell + 0.40 * cell;   // rayon Frank + rayon pomme
	        boolean appleAhead = Math.signum(deltaX) == dir;
	        boolean touching   = Math.abs(deltaX) <= contact + 0.05 * cell;
	        if (!appleAhead || !touching)
	            continue;

	        // La case devant la pomme doit être libre
	        Grid.Position beyond = grid().new Position(ap.x() + dir, ap.y());
	        if (!canReceiveApple(beyond))
	            continue;

	        // La pomme écrase le gazon de la case où elle ARRIVE, puis s'y installe
	        for (Entity o : new ArrayList<>(grid().cellAt(beyond).entities())) {
	            if (o instanceof GrassHigh gh) gh.startFade();
	            if (o instanceof GrassLow gl)  remove(gl);
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
	// CHUTE DES POMMES (délai ~1 s puis chute progressive)
	// ============================================================

	private void updateAppleFall() {
	    for (Entity e : new ArrayList<>(entities())) {
	        if (!(e instanceof Apple apple))
	            continue;

	        Grid.Position pos = apple.position();
	        Grid.Position south = grid().new Position(pos.x(), pos.y() + 1);

	        /*
	         * Support solide :
	         * - mur / bord
	         * - GrassHigh = ligne épaisse
	         * - autre pomme
	         * - fruit / cerise
	         *
	         * Si la pomme rencontre ça, elle s'arrête.
	         */
	        boolean supported =
	                map.isWall(south.x(), south.y())
	                || cellHas(south, GrassHigh.class)
	                || cellHas(south, Apple.class)
	                || cellHas(south, Fruit.class)
	                || cellHas(south, Cherry.class);

	        if (supported) {
	            apple.disarm();
	            apple.disarmLow();
	            apple.setFalling(false);
	            continue;
	        }

	        /*
	         * Ligne fine :
	         * GrassLow n'est pas un vrai support solide.
	         * La pomme s'arrête un petit moment dessus,
	         * puis elle l'écrase et continue à tomber.
	         */
	        if (cellHas(south, GrassLow.class)) {
	            apple.setFalling(true);

	            if (!apple.isLowArmed()) {
	                apple.armLow(LOW_DELAY_TICKS);
	                continue;
	            }

	            apple.tickLowDelay();

	            if (!apple.lowDelayDone())
	                continue;

	            // Le délai est terminé : la pomme écrase la ligne fine.
	            for (Entity occ : new ArrayList<>(grid().cellAt(south).entities())) {
	                if (occ instanceof GrassLow)
	                    remove(occ);
	            }

	            apple.disarmLow();
	            // Pas de continue ici : la pomme va descendre dans cette case plus bas.
	        }

	        /*
	         * Aucun support :
	         * Si la pomme vient de perdre son support, elle attend avant de tomber.
	         * C'est le cas où Frank creuse sous la pomme.
	         */
	        if (!apple.isFalling()) {
	            if (!apple.isArmed()) {
	                apple.arm(FALL_DELAY_TICKS);
	                continue;
	            }

	            apple.tickDelay();

	            if (!apple.delayDone())
	                continue;

	            apple.setFalling(true);
	        }

	        /*
	         * Ralentissement de la chute :
	         * la pomme descend d'une case seulement tous les FALL_STEP_TICKS.
	         */
	        if (tick % FALL_STEP_TICKS != 0)
	            continue;

	        Grid.Cell below = grid().cellAt(south);

	        /*
	         * Collision avec les entités dans la case du dessous.
	         * Important : la pomme ne disparaît PAS après avoir tué.
	         */
	        for (Entity occ : new ArrayList<>(below.entities())) {
	        	if (occ instanceof Frank f) {
	        	    state.killedByEnemy();
	        	    remove(f);
	        	}

	            if (occ instanceof Enemy en) {
	                remove(en);
	                state.addScore(500);
	            }

	            if (occ instanceof GrassLow) {
	                remove(occ);
	            }
	        }

	        apple.setPosition(south);
	    }
	}
	// ============================================================
	// PROJECTILES CERISE
	// ============================================================

	private void removeBadCherryProjectiles() {
		List<Entity> dead = new ArrayList<>();
		for (Entity e : entities()) {
			if (!(e instanceof CherryProjectile cp))
				continue;

			// La case juste devant le projectile, dans sa direction de tir
			Grid.Position next = grid().new Position(cp.position().x() + cp.facingDx(),
					cp.position().y() + cp.facingDy());

			// Mur → la cerise disparaît
			if (map.isWall(next.x(), next.y())) {
				dead.add(cp);
				continue;
			}

			// Tout obstacle dans la case suivante → la cerise disparaît
			for (Entity o : grid().cellAt(next).entities()) {
				if (o instanceof GrassHigh || o instanceof GrassLow || o instanceof Fruit || o instanceof Apple
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
		frank.useCherry();
		int sx = frank.position().x() + frank.facingDx();
		int sy = frank.position().y() + frank.facingDy();
		Grid.Position p = grid().new Position(sx, sy);
		if (map.isWall(p.x(), p.y()))
			return; // ← seule condition : pas tirer dans un mur

		CherryProjectile proj = new CherryProjectile(game, sx, sy, frank.facingDx(), frank.facingDy());
		proj.setStunt(new BasicStunt(proj));
		proj.stunt().drive(frank.facingDx() * CHERRY_SPEED, frank.facingDy() * CHERRY_SPEED);
		add(proj);
	}
	// ============================================================
	// PHYSIQUE
	// ============================================================

	@Override
	public boolean canOccupy(Entity e, Grid.Position target) {

		if (map.isWall(target.x(), target.y()))
			return false;

		if (e instanceof Frank frank) {
			Apple apple = null;
			for (Entity o : grid().cellAt(target).entities())
				if (o instanceof Apple a) {
					apple = a;
					break;
				}

			if (apple != null) {
				int dx = target.x() - frank.position().x();
				int dy = target.y() - frank.position().y();
				if (dy != 0)
					return false;
				Grid.Position beyond = grid().new Position(target.x() + dx, target.y());
				boolean ok = canReceiveApple(beyond);
				System.out.println("[PUSH] Frank veut pousser pomme, case derrière libre ? " + ok + " (beyond="
						+ beyond.x() + "," + beyond.y() + ")");
				return ok;
			}
		}

		if (e instanceof Enemy en) {
		    // une pomme posée (stable) est un obstacle pour TOUS les monstres
		    for (Entity o : grid().cellAt(target).entities())
		        if (o instanceof Apple a && !a.isFalling())
		            return false;

		    if (en.canDig())
		        return true;
		    for (Entity o : grid().cellAt(target).entities())
		        if (o instanceof GrassHigh)
		            return false;
		    return true;
		}

		if (e instanceof CherryProjectile) {
			for (Entity o : grid().cellAt(target).entities())
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
}