package map;

import java.awt.HeadlessException;

import net.ByteManager;

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
	private static final int BOTTOM_LAYERS = 3;
	/* Number of top layers - Must be at least one
	 * (Layers that are drawn over player) */
	private static final int TOP_LAYERS = 2;
	
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
	
	@Optional(value = "")	//Tells network serializer to ignore this variable
	private int width = 0;
	@Optional(value = "")	//Tells network serializer to ignore this variable
	private int height = 0;
	
	private int tilesetId = 0;
	private Tile tiles[][] = null;
	/* Location on megamap */
	private int locX = 0;
	private int locY = 0;
	
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
	
	/* Convert this map to array of bytes */
//	public byte[] getByteData(){
//		byte tmp[] = new byte[ ((BOTTOM_LAYERS + TOP_LAYERS + 1) 
//		                     * (width * height) * 2)
//		                     + 4 + 4 + 4 ];
//		byte tmp4[];
//		tmp4 = ByteManager.intToByte(tilesetId);
//		tmp[0] = tmp4[0]; tmp[1] = tmp4[1]; tmp[2] = tmp4[2]; tmp[3] = tmp4[3];
//		tmp4 = ByteManager.intToByte(locX);
//		tmp[4] = tmp4[0]; tmp[5] = tmp4[1]; tmp[6] = tmp4[2]; tmp[7] = tmp4[3];
//		tmp4 = ByteManager.intToByte(locY);
//		tmp[8] = tmp4[0]; tmp[9] = tmp4[1]; tmp[10] = tmp4[2]; tmp[11] = tmp4[3];
//		
//		byte tmp2[] = new byte[2];
//		for(int i = 0; i < width; i++){
//			for(int j = 0; j < height; j++){
//				
//			}
//		}
//		return tmp;
//	}

//	private byte[] tileToByte(Tile t){
//		byte tmp[] = new byte[(BOTTOM_LAYERS + TOP_LAYERS + 1) * 2];
//		byte tmp2[];
//		
//		tmp2 = ByteManager.shortToByte(t.getGround());
//		int tmpPointer = 0;
//		tmp[0] = tmp2[0]; tmp[1] = tmp2[1];
//		tmpPointer = 2;
//		for(int i = 0; i < BOTTOM_LAYERS; i++){
//			tmp2 = ByteManager.shortToByte(t.getBottom(i));
//			
//		}
//		for(int i = 0; i < TOP_LAYERS; i++){
//			t.getTop(i);
//		}
//	}
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getTilesetId() { return tilesetId; }
}
