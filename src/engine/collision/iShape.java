package engine.collision;

public interface iShape {
	public boolean intersects(iShape shape);
	public boolean intersects(Circle circle);
	public boolean intersects(Rect rect);
}
