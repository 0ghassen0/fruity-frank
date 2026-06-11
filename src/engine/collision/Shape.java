package engine.collision;

import engine.core.ISU;

public abstract class Shape implements iShape{

	private ISU.Coord center;
	
	public Shape(ISU.Coord center) {
		assert center != null;
		this.center = center;
	}
	
    
    public ISU.Coord center() {
        return center;
    }
	
}
