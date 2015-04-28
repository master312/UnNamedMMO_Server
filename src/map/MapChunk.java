package map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.Deflater;

import net.OpCodes;
import net.Packet;
import net.PacketBuilder;

public class MapChunk {
	public enum Layer{
		GROUND, BOTTOM, TOP
	}
	/* Dimensions of one tile, in pixels */
	public static final int TILE_WIDTH = 32;
	public static final int TILE_HEIGHT = 32;
	
	private int width = 0;
	private int height = 0;
	
	private int tilesetId = 0;
	private Tile tiles[][] = null;
	/* Location on megamap */
	private int locX = 0;
	private int locY = 0;
	
	/* Number of players on this map */
	private int playersNum = 0;
	/* Time when this chunk started to timeout */
	private long timeoutStart = 0;
	/* Compressed pacekt of this map, to be send over network */
	private Packet pack = null;
	
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
		generatePacket();
	}
	
	public void fillLayer(int tile, Layer layer, int layerNumber){		
	}
	
	/* Generates compressed packet to be send over network */
	private void generatePacket(){
		PacketBuilder pb = new PacketBuilder();
		pb.writeInt(getTilesetId());
		pb.writeInt(getLocX());
		pb.writeInt(getLocY());
		for(int i = 0; i < getWidth(); i++){
			for(int j = 0; j < getHeight(); j++){
				Tile t = getTile(i, j);
				pb.writeShort(t.getGround());
				for(int g = 0; g < Tile.BOTTOM_LAYERS; g++){
					pb.writeShort(t.getBottom(g));
				}
				for(int g = 0; g < Tile.TOP_LAYERS; g++){
					pb.writeShort(t.getTop(g));
				}
			}
		}
		/* Compress map data */
		Packet tmpPack = pb.getPacket();
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_COMPRESSION);
		compressor.setInput(tmpPack.data);
		compressor.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(tmpPack.data.length);
		byte buf[] = new byte[1024];
	    while (!compressor.finished()) {
	        int count = compressor.deflate(buf);
	        bos.write(buf, 0, count);
	    }
	    try {
	        bos.close();
	    } catch (IOException e) { }
	    byte[] compressedData = bos.toByteArray();
		/* Create map packet */
		pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_MAP_CHUNK);
		pb.writeBytes(compressedData);
		pack = pb.getPacket();
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
	
	
	
	public Packet getPack() { return pack; }

	public Tile getTile(int x, int y){ return tiles[x][y]; }
	
	public long getTimeout() { return timeoutStart; }
	
	public int getLocX() { return locX;}
	public void setLocX(int locX) { this.locX = locX;}
	public int getLocY() { return locY; }
	public void setLocY(int locY) { this.locY = locY; }
	
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public int getTilesetId() { return tilesetId; }
}
