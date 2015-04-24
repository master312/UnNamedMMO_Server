package entities;

public class Player extends Pawn{

	public Player(){
	}
	
	public Player(int id, int locX, int locY, int spriteId){
		this.setId(id);
		this.setLocX(locX);
		this.setLocY(locY);
		this.setSpriteId(spriteId);
		this.setCurrentSpeed(10);
	}
	
}
