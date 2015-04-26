package map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.NetProtocol;
import main.PlayerHandler;
import main.PlayerHandler.NeedMapChunk;

import com.esotericsoftware.minlog.Log;

//TODO: Chunks timeout

public class MapManager {
	//TODO: Move these static variables to Common class
	
	/* These values will be used if world width and height are not specified */
	private static final int DEFAULT_WORLD_WIDTH = 100;
	private static final int DEFAULT_WORLD_HEIGHT = 100;
	/* These values will be used if chunk width and height are not specified */
	private static final int DEFAULT_CHUNK_WIDTH = 50;
	private static final int DEFAULT_CHUNK_HEIGHT = 50;
	/* Chunks clear interval. Chunks that are no longer needed are deleted
	 * on this interval (in MS) */
	private static final long CHUNKS_CLEAR_INTERVAL = 15000;
	private long lastClearTime = 0;
	
	private int worldWidth = DEFAULT_WORLD_WIDTH;
	private int worldHeight = DEFAULT_WORLD_HEIGHT;
	private int chunkWidth = DEFAULT_CHUNK_WIDTH;
	private int chunkHeight = DEFAULT_CHUNK_HEIGHT;
	
	/* List of tilesets ID's */
	private ArrayList<Integer> tilesets = new ArrayList<Integer>();
	/* Array of all chunks in the world */
	private MapChunk chunks[][];
	/* List of loaded chunks */
	private ArrayList<Point> chunksLoaded = new ArrayList<Point>();
	
	public MapManager(){ }
	
	public MapManager(int _worldWidth, int _worldHeight){
		worldWidth = _worldWidth;
		worldHeight = _worldHeight;
	}
	
	public void initWorld(){
		chunks = new MapChunk[worldWidth][worldHeight];
		for(int i = 0; i < worldWidth; i++){
			for(int j = 0; j < worldHeight; j++){
				chunks[i][j] = null;
			}
		}
		
		Log.info("MapManager: World initialized. " + 
				"World size: " + worldWidth + "x" + worldHeight);
	}
	
	/* Convert pixels coordinates, to chunk coordinates */
	public Point pixelToChunk(int x, int y){
		Point tmpPoint = pixelToTile(x, y);
		tmpPoint.x = tmpPoint.x / chunkWidth;
		tmpPoint.y = tmpPoint.y / chunkHeight;
		return tmpPoint;
	}
	
	/* Converts pixel coordinates, to tile coordinates (on world map) */
	public Point pixelToTile(int x, int y){
		return new Point((int)(x / MapChunk.TILE_WIDTH), 
						(int)(y / MapChunk.TILE_HEIGHT));
	}
	
	/* Delete not needed chunks from memory */
	private void clearChunks(){
		boolean isCleared = false;

		if(isCleared)
			System.gc();
	}
	
	/* Creates new chunk at coordinates x:y */
	private void createNewChunk(int x, int y){
		chunks[x][y] = new MapChunk(chunkWidth, chunkHeight, x, y);
		chunks[x][y].initEmpty();
		chunksLoaded.add(new Point(x, y));
		Log.info("MapManager: New chunk generated");
	}
	
	/* Returns chunk. If chunk is not loaded/created this function will
	 * load/create it. Returns NULL if out of range */
	public MapChunk getChunk(int x, int y){
		if(x >= worldWidth || y >= worldHeight)
			return null;
		if(chunks[x][y] == null){
			//TODO: Load chunk here
			createNewChunk(x, y);
		}
		return chunks[x][y];
	}

	/* Sends new chunks to player */
	public void sendChunks(PlayerHandler pHandler){
		List<NeedMapChunk> needed = pHandler.getNeedChunks();
		for(int i = 0; i < needed.size(); i++){
			NeedMapChunk ch = needed.get(i);
			if(!ch.isSend){
				NetProtocol.srMapChunk(pHandler, getChunk(ch.x, ch.y));
				ch.isSend = true;
			}
		}
	}
	
	public int getWorldWidth(){
		return worldWidth;
	}
	
	public int getWorldHeight(){
		return worldHeight;
	}
}
