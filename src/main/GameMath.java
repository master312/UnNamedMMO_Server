package main;

import entities.Entity;

public class GameMath {
	
	/* Check if those two entities are in range of visibility 
	 * Range is defined in common class*/
	public static boolean isInVisibleRange(Entity e1, Entity e2){
		return getEntityDistance(e1, e2) < Common.ENTITY_VIEW_DISTANCE;
	}
	
	/* Return distance between 2 entities */
	public static int getEntityDistance(Entity e1, Entity e2){
		return getPointDistance((int)e1.getLocX(), (int)e1.getLocY(),
								(int)e2.getLocX(), (int)e2.getLocY());
	}
	
	/* Return distance in pixels between two points */
	public static int getPointDistance(int x1, int y1, int x2, int y2){
		return (int)Math.sqrt((x1 - x2) * (x1 - x2) + 
							  (y1 - y1) * (y1 - y1));
	}
}
