package engine.graphics;

import oop.graphics.Canvas;
import oop.graphics.Graphics;

/**
 * Entry point for the rendering pipeline.
 *
 * {@code Painter} is abstract because background rendering is game-specific —
 * subclasses must implement {@link #paintBackground(Canvas, Graphics)}. The
 * delegation chain (background → avatars) is fixed by this class:
 *
 */
public abstract class Painter implements Canvas.PaintListener {

	protected final View view;

	protected Painter(View view) {
		this.view = view;
	}

	// -------------------------------------------------------------------------
	// Canvas.PaintListener
	// -------------------------------------------------------------------------

	@Override
	public void paint(Canvas canvas, Graphics g) {
		paintBackground(canvas, g);
		view.paint(g);
		paintForeground(canvas, g);
	}

	/**
	 * Paints the background before any avatar is drawn. Implement this to fill,
	 * clear, or draw the game's background layer.
	 */
	protected abstract void paintBackground(Canvas canvas, Graphics g);

	/**
	 * Paints an overlay on top of all avatars (HUD, minimap, …). Default no-op.
	 */
	protected void paintForeground(Canvas canvas, Graphics g) {
	}

	/**
	 * Called when the canvas first becomes visible. Override to start a
	 * {@code RenderLoop} or initialize graphics resources.
	 */
	@Override
	public void visible(Canvas canvas) {
	}

	/**
	 * Called when the canvas is no longer visible. Override to pause a
	 * {@code RenderLoop} or release graphics resources.
	 */
	@Override
	public void revoked(Canvas canvas) {
	}
}