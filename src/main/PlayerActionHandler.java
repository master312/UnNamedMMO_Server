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
		case CHAT:
			handlePlayerChat(pl, act);
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
		boolean toSendDir = false;
		if(entity.getDir() != dir){
			//Send direction and position update
			entity.setDir(dir);
			NetProtocol.srPawnUpdatePosDir(pl, entity);
			toSendDir = true;
		}else{
			//Send new position back to player
			NetProtocol.srPawnUpdatePosition(pl, entity);
		}
		
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
			PlayerHandler ph = ((Player) tmpEntity).getPlayerHandler();
			if(!toSendDir){
				NetProtocol.srPawnUpdatePosition(ph, entity);
			}else{
				NetProtocol.srPawnUpdatePosDir(ph, entity);
			}
		}
	}
	
	private static void handlePlayerChat(PlayerHandler pl, PlayerAction act){
		short msgType = (short)act.values.get(0);
		String msg = (String)act.values.get(1);
		
		//TODO: Some cheat and anti-spam check here
		//But better do it in action generator, or even in PlayerHandler
		
		//Return message to player
		NetProtocol.srTextMsg(pl, msgType, pl.getName(), msg);
		
		for(int i = 0; i < pl.getEntitiesInRange().size(); i++){
			//Relay message back to all visible clients
			Entity tmpEntity = pl.getEntitiesInRange().get(i);
			if(!tmpEntity.isPlayer()){
				continue;
			}
			NetProtocol.srTextMsg(((Player) tmpEntity).getPlayerHandler(), 
								  msgType, pl.getName(), msg);
		}
	}
}
