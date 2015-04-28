package map;

public class Tile{
	/* Number of bottom layers, excluding ground - Must be at least one
	 * (Layers that are drawn under player) */
	public static final int BOTTOM_LAYERS = 3;
	/* Number of top layers - Must be at least one
	 * (Layers that are drawn over player) */
	public static final int TOP_LAYERS = 2;
	
	private short ground = -1;
	private short bottomTile[];
	private short topTile[];
	
	public Tile(){
		initLayers();
	}
	
	public Tile(short _ground){
		ground = _ground;
		initLayers();
	}
	
	private void initLayers(){
		bottomTile = new short[BOTTOM_LAYERS];
		topTile = new short[TOP_LAYERS];
		for(int i = 0; i < BOTTOM_LAYERS; i++){
			bottomTile[i] = -1;
		}
		for(int i = 0; i < TOP_LAYERS; i++){
			topTile[i] = -1;
		}
	}
	
	/* Return tile on ground layer */
	public short getGround() { return ground; }
	/* Return tile from bottom layer[num] */
	public short getBottom(int num) { return bottomTile[num]; }
	/* Return tile from top layer[num] */
	public short getTop(int num) { return topTile[num]; }
	
	public void setGround(short _tile) { ground = _tile; }
	public void setBottom(int num, short _tile) { bottomTile[num] = _tile; }
	public void setTop(int num, short _tile) { topTile[num] = _tile; }
}