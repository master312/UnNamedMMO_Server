package main;

import net.NetProtocol;
import entities.Entity;
import entities.Entity.Direction;
import entities.Player;
import main.PlayerHandler.PlayerAction;

public class PlayerActionHandler {
	/* Maximum player actions pre tick
	 * All actions above this number will be droped */
	private static final int MAXIMUM_ACTIONS = 6;
	private static int delta = 0;
	
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
	
	private static void handlePlayerAction(PlayerHandler pl, PlayerAction act){
		switch(act.type){
		case MOVE:
			handlePlayerMovement(pl, (Direction)act.values.get(0));
			break;
		default:
			break;
		}
	}
	
	private static void handlePlayerMovement(PlayerHandler pl, Direction dir){
		Player entity = pl.getCharacter();
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
		NetProtocol.srPawnUpdatePosition(pl, entity);
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
