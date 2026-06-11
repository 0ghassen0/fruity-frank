package engine.collision;

import java.util.HashSet;
import java.util.Set;

public class Bounding {
	private Set<iShape> shapes = new HashSet<>();

	public void add(iShape s) {
		assert s != null;
		shapes.add(s);
	}
	
	
	// pour éviter d'écrire plusieurs .add à la main
	public void add(iShape[] shapes) {
		assert shapes != null;
		for(iShape s : shapes) {
			this.add(s);
		}
	}

	public boolean intersects(iShape shape) {
		for (iShape s : shapes) {
			if (s.intersects(shape))
				return true;
		}

		return false;
	}

	// à optimiser
	public boolean intersects(Bounding b) {
		for (iShape s1 : shapes) {
			for (iShape s2 : b.shapes) {
				if (s1.intersects(s2))
					return true;

			}
		}
		
		return false;
	}
	
	
}
