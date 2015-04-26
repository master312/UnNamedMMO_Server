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
	public static class NetDirection {
		/* Direction ID's over network */
		public static final int DIRECTION_NORT = 0;
		public static final int DIRECTION_NORTHEAST = 1;
		public static final int DIRECTION_EAST = 2;
		public static final int DIRECTION_SOUTHEAST = 3;
		public static final int DIRECTION_SOUTH = 4;
		public static final int DIRECTION_SOUTHWEST = 5;
		public static final int DIRECTION_WEST = 6;
		public static final int DIRECTION_NORTHWEST = 7;
	}
	public enum EntityType{
		UNDEFINED{ public String toString(){ return "UNDEFINED"; } }, 
		MONSTER{ public String toString(){ return "MONSTER"; } },
		NPC{ public String toString(){ return "NPC"; } },
		PLAYER{ public String toString(){ return "PLAYER"; } }
	}
	
	private EntityType type = EntityType.UNDEFINED;
	private int id = -1;
	private float locX = 0f, locY = 0f; 
	private int width = 0, height = 0;
	private int spriteId = -1;
	private String name = "";
	
	
	public Entity(){
	}
	
	public boolean isMonster() { return type == EntityType.MONSTER; }
	public boolean isNpc() { return type == EntityType.NPC; }
	public boolean isPlayer() { return type == EntityType.PLAYER; }
	public boolean isPawn() { return isMonster() || isNpc() || isPlayer(); }
	
	public void move(float x, float y){
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

	public float getLocX() {
		return locX;
	}

	public void setLocX(float locX) {
		this.locX = locX;
	}

	public float getLocY() {
		return locY;
	}

	public void setLocY(float locY) {
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
	
	public boolean equals(Object o){
		return id == ((Entity) o).getId();
	}
	
}
