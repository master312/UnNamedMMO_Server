package entities;

/* This is base class for every entity in game */
public class Entity {
	public enum Direction{
		NORTH{ public String toString(){ return "NORTH"; } }, 
		NORTHEAST{ public String toString(){ return "NORTHEAST"; } }, 
		EAST{ public String toString(){ return "EAST";}	}, 
		SOUTHEAST { public String toString() { return "SOUTHEAST"; } }, 
		SOUTH { public String toString() { return "SOUTH"; } }, 
		SOUTHWEST { public String toString() { return "SOUTHWEST"; } }, 
		WEST { public String toString() { return "WEST"; } }, 
		NORTHWEST { public String toString() { return "NORTHWEST"; } }
	}
	public enum EntityType{
		UNDEFINED, MONSTER, NPC, PLAYER
	}
	
	private EntityType type = EntityType.UNDEFINED;
	private int id = -1;
	private int locX = 0, locY = 0, width = 0, height = 0;
	private int spriteId = -1;
	private String name = "";
	
	
	public Entity(){
	}
	
	public boolean isMonster() { return type == EntityType.MONSTER; }
	public boolean isNpc() { return type == EntityType.NPC; }
	public boolean isPlayer() { return type == EntityType.PLAYER; }

	
	public void move(int x, int y){
		locX += x;
		locY += y;
	}
	
	public EntityType getType() {
		return type;
	}

	public void setType(EntityType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLocX() {
		return locX;
	}

	public void setLocX(int locX) {
		this.locX = locX;
	}

	public int getLocY() {
		return locY;
	}

	public void setLocY(int locY) {
		this.locY = locY;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getSpriteId() {
		return spriteId;
	}

	public void setSpriteId(int spriteId) {
		this.spriteId = spriteId;
	}
	
	
	
}
