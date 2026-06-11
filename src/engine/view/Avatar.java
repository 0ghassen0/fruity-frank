package engine.view;

import engine.entity.Entity;
import oop.graphics.Graphics;

public abstract class Avatar {

	protected final Entity entity;
	protected final View view;

	public Avatar(View view, Entity entity) {
		
		assert view != null;
		assert entity != null;
		this.view = view;
		this.entity = entity;
		
		entity.setAvatar(this);
		
	}
	
	
	public abstract void paint(Graphics g);
	
}
