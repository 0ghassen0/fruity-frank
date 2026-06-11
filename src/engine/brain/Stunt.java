package engine.brain;

import engine.core.Grid;
import engine.core.Model;
import engine.entity.Entity;

public abstract class Stunt {
	
	protected final Model model;
	protected final Entity entity;
	protected Bot bot;
	
	public Stunt(
			Model model,
			Entity entity)
	{
		assert model != null;
		assert entity != null;
		
		this.model = model;
		this.entity = entity;
		
		entity.setStunt(this);
	}
	
	// GETTERS
	
	public Entity entity() {
		return entity;
	}
	
	public Bot bot() {
		return this.bot;
	}
	
	public void setBot(Bot b) {
		this.bot = b;
	}
	
	// entite arrive à destination : appelée par le modele
	
	public void set(double x, double y){
		if(bot != null) bot.done();
	}
	
	public void set(Grid.Cell c) {}
	
	public void set(int orientation) {}
	
	// appelé par le modele quand collision
	
	public void collision(Entity other) {
		entity.stopMoving();
		if(bot != null) bot.collision(other);
	}
	
	
}














