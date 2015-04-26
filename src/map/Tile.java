package map;

public class Tile{
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
		bottomTile = new short[MapChunk.BOTTOM_LAYERS];
		topTile = new short[MapChunk.TOP_LAYERS];
		for(int i = 0; i < MapChunk.BOTTOM_LAYERS; i++){
			bottomTile[i] = -1;
		}
		for(int i = 0; i < MapChunk.TOP_LAYERS; i++){
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