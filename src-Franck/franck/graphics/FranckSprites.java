package franck.graphics;

import franck.entities.Enemy;
import franck.entities.Fruit;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public final class FranckSprites {

	private static final String PLAYER = "sprites/Fruity_Frank-player.png"; // 100x76
	private static final String ENEMY = "sprites/Fruity_Frank-enemies.png"; // 148x136
	private static final String ITEMS = "sprites/Fruity_Frank-Items.png"; // 124x64

	// Géométrie EXACTE mesurée sur les images :
	// premier sprite à (4,5), taille 20x15, pas horizontal 24, pas vertical 20
	private static final int OX = 4, OY = 5; // origine
	private static final int SW = 20, SH = 15; // taille d'un sprite
	private static final int STEP_X = 24, STEP_Y = 20;

	private final BufferedImage[] frank = new BufferedImage[12]; // 4 col x 3 lignes
	private final BufferedImage[] enemy = new BufferedImage[24]; // 6 col x 4 lignes
	private final BufferedImage[] item = new BufferedImage[15]; // 5 col x 3 lignes

	private boolean loaded = false;

	public void ensureLoaded(Graphics g) {
		if (loaded)
			return;

		BufferedImage ps = g.load(PLAYER);
		BufferedImage es = g.load(ENEMY);
		BufferedImage is = g.load(ITEMS);

		// FRANK : 4 colonnes × 3 lignes
		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 4; c++)
				frank[r * 4 + c] = cell(ps, c, r);

		// ENNEMIS : 6 colonnes × 4 lignes
		for (int r = 0; r < 4; r++)
			for (int c = 0; c < 6; c++)
				enemy[r * 6 + c] = cell(es, c, r);

		// ITEMS : 5 colonnes × 3 lignes
		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 5; c++)
				item[r * 5 + c] = cell(is, c, r);

		loaded = true;
	}

	/** Découpe le sprite (colonne c, ligne r) avec la géométrie exacte mesurée. */
	private BufferedImage cell(BufferedImage sheet, int c, int r) {
		int x = OX + c * STEP_X;
		int y = OY + r * STEP_Y;
		if (sheet == null)
			return null;
		if (x + SW > sheet.getWidth() || y + SH > sheet.getHeight())
			return null;
		try {
			return sheet.getSubimage(x, y, SW, SH);
		} catch (Exception e) {
			return null;
		}
	}

	// FRANK : dir 0=droite 1=gauche 2=haut 3=bas, animation 4 frames
	public BufferedImage frank(int dir, int frame) {
		int row = switch (dir) {
		case 0 -> 1; // droite
		case 1 -> 0; // gauche
		case 2 -> 2; // haut (pas de sprite dos → on prend le face)
		case 3 -> 2; // bas (face)
		default -> 2;
		};
		return frank[row * 4 + (frame % 4)];
	}

	// ENEMY : ligne = type, colonnes par direction (0-1 droite, 2-3 gauche, 4-5 vertical)
	// dir : 0=droite 1=gauche 2=haut 3=bas
	public BufferedImage enemy(Enemy.EnemyType type, int dir, int frame) {
	    int row = switch (type) {
	        case ORANGE -> 0;
	        case PURPLE -> 1;
	        case GREEN  -> 2;
	        case RED    -> 3;
	    };
	    int baseCol = switch (dir) {
	        case 0 -> 1;  // droite → colonnes 0-1
	        case 1 -> 0;  // gauche → colonnes 2-3
	        case 2 -> 4;  // haut   → colonnes 4-5
	        case 3 -> 4;  // bas    → colonnes 4-5
	        default -> 0;
	    };
	    int col = baseCol + (frame % 2); // anime entre les 2 frames de la direction
	    return enemy[row * 6 + col];
	}

	// FRUITS — index identifiés par analyse des couleurs
	public BufferedImage fruit(Fruit.FruitType type) {
		return switch (type) {
		case BANANA -> item[5]; // banane jaune
		case ORANGE -> item[1]; // fruit rouge/orange
		case STRAWBERRY -> item[8]; // fraise rouge
		case PLUM -> item[6]; // prune violette
		case GRAPES -> item[10]; // raisin
		};
	}

	public BufferedImage apple() {
		return item[12];
	} // grosse pomme orange/rouge

	public BufferedImage cherry() {
		return item[4];
	} // cerises rouge foncé

	public boolean isLoaded() {
		return loaded;
	}
}