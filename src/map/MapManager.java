package map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.NetProtocol;
import main.Common;
import main.PlayerHandler;
import main.PlayerHandler.NeedMapChunk;

import com.esotericsoftware.minlog.Log;

//TODO: Chunks timeout

public class MapManager {

	/* These values will be used if world width and height are not specified */
	private static final int DEFAULT_WORLD_WIDTH = 100;
	private static final int DEFAULT_WORLD_HEIGHT = 100;
	/* These values will be used if chunk width and height are not specified */
	private static final int DEFAULT_CHUNK_WIDTH = 50;
	private static final int DEFAULT_CHUNK_HEIGHT = 50;
	private long lastClearTime = 0;
	
	private int worldWidth = DEFAULT_WORLD_WIDTH;
	private int worldHeight = DEFAULT_WORLD_HEIGHT;
	private int chunkWidth = DEFAULT_CHUNK_WIDTH;
	private int chunkHeight = DEFAULT_CHUNK_HEIGHT;
	
	/* Array of all chunks in the world */
	private MapChunk chunks[][];
	/* List of loaded chunks */
	private ArrayList<Point> chunksLoaded = new ArrayList<Point>();
	
	public MapManager(){ }
		
	public void initWorld(int _worldWidth, int _worldHeight){
		if(_worldWidth > 0 || _worldHeight > 0){
			worldWidth = _worldWidth;
			worldHeight = _worldHeight;
		}
		chunks = new MapChunk[worldWidth][worldHeight];
		for(int i = 0; i < worldWidth; i++){
			for(int j = 0; j < worldHeight; j++){
				chunks[i][j] = null;
			}
		}
		
		Log.info("MapManager: World initialized. " + 
				"World size: " + worldWidth + "x" + worldHeight);
	}
	
	/* Called on interval */
	public void update(){
		clearChunks();
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
		if(System.currentTimeMillis() - lastClearTime < 
				Common.CHUNKS_CLEAR_INTERVAL)
			return;
		boolean isCleared = false;
		lastClearTime = System.currentTimeMillis();
		
		for(int i = 0; i < chunksLoaded.size(); i++){
			//CHUNK_TIMEOUT
			MapChunk tmpCh = chunks[chunksLoaded.get(i).x]
									[chunksLoaded.get(i).y];
			if(tmpCh == null){
				chunksLoaded.remove(i);
				i = (i > 0) ? i -- : 0;
				continue;
			}
			if(tmpCh.getTimeout() > 0){
				//Chunk's timeout has started
				if(System.currentTimeMillis() - tmpCh.getTimeout() 
						> Common.CHUNK_TIMEOUT){
					//Chunk has timeouted
					chunks[chunksLoaded.get(i).x][chunksLoaded.get(i).y] = null;
					chunksLoaded.remove(i);
					i = (i > 0) ? i -- : 0;
					isCleared = true;
				}
			}
		}
		
		if(isCleared)
			System.gc();
	}
	
	/* Creates new chunk at coordinates x:y */
	private void createNewChunk(int x, int y){
		chunks[x][y] = new MapChunk(chunkWidth, chunkHeight, x, y);
		chunks[x][y].initEmpty();
		chunksLoaded.add(new Point(x, y));
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
		List<NeedMapChunk> tmpL = pHandler.getNeedChunks();
		for(int i = 0; i < tmpL.size(); i++){
			NeedMapChunk tmpC = tmpL.get(i);
			if(!tmpC.isSend){
				NetProtocol.srMapChunk(pHandler, getChunk(tmpC.x, tmpC.y));
				addPlayerToChunk(tmpC.x, tmpC.y);
				tmpC.isSend = true;
			}
		}
	}
	
	/* Adds player to chunk */
	public void addPlayerToChunk(int x, int y){
		if(chunks[x][y] == null){
			Log.warn("MapManager: Tryed to add player to non-existing chunk");
			return;
		}
		chunks[x][y].addPlayer();
	}
	
	public void rempvePlayerFromChunk(int x, int y){
		if(chunks[x][y] == null){
			Log.warn("MapManager: Tryed to remove player to non-existing chunk");
			return;
		}
		chunks[x][y].removePlayer();
	}
	
	public int getWorldWidth(){ return worldWidth; }
	public int getWorldHeight(){ return worldHeight; }
	
	public int getChunkWidth() { return chunkWidth; }
	public int getChunkHeight() { return chunkHeight; }
}
