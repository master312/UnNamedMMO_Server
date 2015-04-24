package entities;

public class Player extends Pawn{

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
	
}
