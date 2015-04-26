package main;

import main.PlayerAction.PlayerActionType;
import net.Packet;
import entities.Entity;
import entities.Entity.NetDirection;

/* This class is used to create PlayerAction objects from raw packets 
 * Packet's OpCode MUST be readed before using functions from this class */
public class PlayerActionGenerator {
	
	/* Generate player movement action from packet */
	public static PlayerAction genMovement(Packet pack){
		PlayerAction tmpAction = new PlayerAction(PlayerActionType.MOVE);
		switch(pack.readShort()){
		//Switch direction
		case NetDirection.DIRECTION_NORT:
			tmpAction.values.add(Entity.Direction.NORTH);
			break;
		case NetDirection.DIRECTION_NORTHEAST:
			tmpAction.values.add(Entity.Direction.NORTHEAST);
			break;
		case NetDirection.DIRECTION_EAST:
			tmpAction.values.add(Entity.Direction.EAST);
			break;
		case NetDirection.DIRECTION_SOUTHEAST:
			tmpAction.values.add(Entity.Direction.SOUTHEAST);
			break;
		case NetDirection.DIRECTION_SOUTH:
			tmpAction.values.add(Entity.Direction.SOUTH);
			break;
		case NetDirection.DIRECTION_SOUTHWEST:
			tmpAction.values.add(Entity.Direction.SOUTHWEST);
			break;
		case NetDirection.DIRECTION_WEST:
			tmpAction.values.add(Entity.Direction.WEST);
			break;
		case NetDirection.DIRECTION_NORTHWEST:
			tmpAction.values.add(Entity.Direction.NORTHWEST);
			break;
		}
		return tmpAction;
	}
}
