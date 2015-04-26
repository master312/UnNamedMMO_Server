package map;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;

public class MapChunk {
	public enum Layer{
		GROUND, BOTTOM, TOP
	}
	/* Dimensions of one tile, in pixels */
	public static final int TILE_WIDTH = 32;
	public static final int TILE_HEIGHT = 32;
	/* Number of bottom layers, excluding ground - Must be at least one
	 * (Layers that are drawn under player) */
	public static final int BOTTOM_LAYERS = 3;
	/* Number of top layers - Must be at least one
	 * (Layers that are drawn over player) */
	public static final int TOP_LAYERS = 2;
	
	//@Optional(value = "")	//Tells network serializer to ignore this variable
	private int width = 0;
	private int height = 0;
	
	private int tilesetId = 0;
	private Tile tiles[][] = null;
	/* Location on megamap */
	private int locX = 0;
	private int locY = 0;
	
	/* Number of players on this map */
	@Optional(value = "")	//Tells network serializer to ignore this variable
	private int playersNum = 0;
	/* Time when this chunk started to timeout */
	@Optional(value = "")
	private long timeoutStart = 0;
	
	public MapChunk() { }
	
	public MapChunk(int _width, int _height, int _x, int _y){
		width = _width;
		height = _height;
		locX = _x;
		locY = _y;
	}
	
	public void initEmpty(){
		tiles = new Tile[width][height];

		for(int i = 0; i < width; i++){
			for(int j = 0; j < height; j++){
				if(i == 0 || j == 0){
					tiles[i][j] = new Tile();
				}else{
					tiles[i][j] = new Tile((short)8);
				}
			}
		}
	}
	
	public void fillLayer(int tile, Layer layer, int layerNumber){		
	}
	
	public void addPlayer() { 
		playersNum ++; 
		timeoutStart = 0;
	}
	public void removePlayer(){
		playersNum --;
		if(playersNum <= 0){
			playersNum = 0;
			timeoutStart = System.currentTimeMillis();
		}
	}
	
	public long getTimeout() { return timeoutStart; }
	
	public int getLocX() { return locX;}
	public void setLocX(int locX) { this.locX = locX;}
	public int getLocY() { return locY; }
	public void setLocY(int locY) { this.locY = locY; }
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getTilesetId() { return tilesetId; }
}
