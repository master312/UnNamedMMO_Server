package entities;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

import main.PlayerHandler;

public class Player extends Pawn{

	@Optional(value = "")	//Tells network serializer to ignore this variable
	private PlayerHandler playerHandler = null;
	
	public Player(){
		this.setType(EntityType.PLAYER);
	}
	
	public Player(int id, int locX, int locY, int speed, int spriteId){
		this.setId(id);
		this.setLocX(locX);
		this.setLocY(locY);
		this.setSpriteId(spriteId);
		this.setCurrentSpeed(speed);
		this.setType(EntityType.PLAYER);
	}
	
	public void setPlayerHandler(PlayerHandler ph){
		playerHandler = ph;
	}
	
	public PlayerHandler getPlayerHandler(){
		return playerHandler;
	}
}
