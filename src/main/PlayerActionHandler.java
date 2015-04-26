package main;

import java.awt.Point;

import map.MapManager;
import net.NetProtocol;
import entities.Entity;
import entities.Entity.Direction;
import entities.Player;

public class PlayerActionHandler {
	/* Maximum player actions pre tick
	 * All actions above this number will be droped */
	private static final int MAXIMUM_ACTIONS = 6;
	private static int delta = 0;
	
	/* Handle all player's (pl) actions */
	public static void handlePlayerActions(PlayerHandler pl, int _delta){
		delta = _delta;
		if(pl.getActionsCount() == 0){
			return;
		}
		for(int i = 0; i < MAXIMUM_ACTIONS; i++){
			PlayerAction tmpAction = pl.getAction();
			if(tmpAction == null)
				break;
			handlePlayerAction(pl, tmpAction);
		}
		//TODO: Warning or kick player if action overflow
		pl.clearActions();	
	}
	
	/* Handle specific action for pl */
	private static void handlePlayerAction(PlayerHandler pl, PlayerAction act){
		switch(act.type){
		case MOVE:
			handlePlayerMovement(pl, (Direction)act.values.get(0));
			break;
		default:
			break;
		}
	}
	
	/* Handle movement action */
	private static void handlePlayerMovement(PlayerHandler pl, Direction dir){
		Player entity = pl.getCharacter();
		MapManager tmpM = Common.getMapManagerSt();
		Point oldChunk = tmpM.pixelToChunk((int)entity.getLocX(),
											(int)entity.getLocY());
		float speed = ((float)entity.getCurrentSpeed() / 100) * delta;
		switch(dir){
		case NORTH:
			entity.move(0, -speed);
			break;
		case NORTHEAST:
			entity.move(speed, -speed);
			break;
		case EAST:
			entity.move(speed, 0);
			break;
		case SOUTHEAST:
			entity.move(speed, speed);
			break;
		case SOUTH:
			entity.move(0, speed);
			break;
		case SOUTHWEST:
			entity.move(-speed, speed);
			break;
		case WEST:
			entity.move(-speed, 0);
			break;
		case NORTHWEST:
			entity.move(-speed, -speed);
			break;
		}
		//Send new position back to player
		NetProtocol.srPawnUpdatePosition(pl, entity);
		
		//Checking if player has moved to new chunk
		Point newChunk = tmpM.pixelToChunk((int)entity.getLocX(), 
											(int)entity.getLocY());
		if(newChunk.x != oldChunk.x || newChunk.y != oldChunk.y){
			//Player moved to new chunk
			pl.calcNeededChunks();
		}
		
		//And to all players around him
		for(int i = 0; i < pl.getEntitiesInRange().size(); i++){
			Entity tmpEntity = pl.getEntitiesInRange().get(i);
			if(!tmpEntity.isPlayer()){
				continue;
			}
			NetProtocol.srPawnUpdatePosition(
									((Player) tmpEntity).getPlayerHandler(), 
									entity);
		}
	}
}
