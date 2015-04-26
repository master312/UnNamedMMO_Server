package main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.AccountsManager.Account;
import main.CharactersManager.AccountCharacters;
import map.MapManager;
import net.NetProtocol;
import net.OpCodes;
import net.Packet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import entities.Entity;
import entities.Player;

/* This class contains all player data
 * and handles packet sending/receiving 
 * Client object, packets objects .... 
 * WARNING: This class object is added to 'preGamePlayers' list in common
 * on its creation. Don't forget to delete it */
public class PlayerHandler extends Listener{
	public enum PlayerState{
		CONNECTED, LOGGED_IN, CHARACTER_SCREEN, IN_GAME, DISCONNECTED
	}
	
	public enum CharacterCreateStatus{
		OK, INVALID_NAME, NOT_ALLOWED
	}
	
	/* Map chunk that player needs to have */
	public class NeedMapChunk{
		/* Has this chunk been send to player */
		public boolean isSend;
		/* Locations of chunk, on megamap (in chunks) */
		public int x, y;
		
		public NeedMapChunk(){
			x = y = 0;
			isSend = false;
		}
		
		public NeedMapChunk(int _x, int _y){
			x = _x;
			y = _y;
		}
		
		public boolean equals(Object o){
			return x == ((NeedMapChunk)o).x && y == ((NeedMapChunk)o).y;
		}
	}
	
	/* Player network client object */
	private Connection client = null;
	/* Current player state */
	private PlayerState state = null;
	/* Time when this player was disconnected (if disconnected) */
	private long disconnectTime = 0;
	/* Players user name and password */
	private Account account = null;
	/* Player character */
	private Player character = null;
	/* List of entities that are in range of this player */
	private List<Entity> entitiesInRange = 
			Collections.synchronizedList(new ArrayList<Entity>());
	/* List of action requested by player, waiting to be executed */
	private List<PlayerAction> actions = 
			Collections.synchronizedList(new ArrayList<PlayerAction>());
	/* List of map chunk positions this player needs */
	private List<NeedMapChunk> neededChunks = 
			Collections.synchronizedList(new ArrayList<NeedMapChunk>());
	/* Whether new map chunks are needed to be send to player */
	private boolean newMapChunks = false;
	
	
	public PlayerHandler(Connection cl){
		client = cl;
		client.setTimeout(Common.CONNECTION_TIMEOUT);
		client.addListener(this);
		client.setKeepAliveTCP(Common.CONNECTION_TIMEOUT / 2);
		state = PlayerState.CONNECTED;
		
		NetProtocol.srLoginReady(this);	//Sends ready signal to client
	}
	
	/* Called when when data is received */
	public void received(Connection c, Object p){
		if(p instanceof Packet){
			Packet tmpP = (Packet) p;
			tmpP.resetPointer();
			if(state != PlayerState.IN_GAME){
				handlePreGameNetwork(tmpP);
			}else if(state == PlayerState.IN_GAME){
				handleInGameNetwork(tmpP);
			}
		}else if(p instanceof Player){
			//Player created new character
			if(state != PlayerState.CHARACTER_SCREEN){
				//Characters can ONLY be created on character screen state
				return;
			}
			handleNewCharacter((Player) p);
		}
	}
	
	/* Called when player disconnects */
	public void disconnected(Connection connection){
		disconnect();
	}

	/* Handle packets used before player switch to in game state */
	public void handlePreGameNetwork(Packet pack){
		if(pack.data.length < 2){
			Log.warn("Invalid packet received from client " + client.getID());
			return;
		}
		short opCode = pack.readShort(); 
		switch(opCode){
		case OpCodes.CL_LOGIN:
			//Login packet
			account = Common.getAccountsManagerSt().new Account();
			account.setUsername(pack.readString());
			account.setPassword(pack.readString());
			if(Common.getAccountsManagerSt().handleLogin(this)){
				//Login OK
				state = PlayerState.LOGGED_IN;
			}else{
				//Login fail
				disconnect();
			}
			character = null;
			break;
		case OpCodes.CL_ENTER_WORLD:
			//Player entered world
			int charId = pack.readInt();
			character = Common.getCharactersManagerSt()
												.getCharacter(account, charId);
			if(character == null){
				Log.error("Invalid character ID received from player");
				disconnect();
			}else{
				Common.removePlayerSt(this);	//Remove player from preGame list
				state = PlayerState.IN_GAME;
				Common.addPlayerToListSt(this); //Add player to inGame list
				character.setPlayerHandler(this);
				calcNeededChunks();
			}
			break;
		}
	}
	
	/* Handle packets used inGame */
	public void handleInGameNetwork(Packet pack){
		short opCode = pack.readShort();
		switch(opCode){
		case OpCodes.CL_MOVE:
			actions.add(PlayerActionGenerator.genMovement(pack));
			break;
		}
	}
	
	/* Handle creation of new character */
	private void handleNewCharacter(Player character){
		CharactersManager cm = Common.getCharactersManagerSt();
		AccountCharacters ac = cm.getAccCharacters(account);
		if(ac.getCharactersCount() < Common.MAXIMUM_ACC_CHAR_NUMBER){
			//If there is space on account for new character
			if(!cm.createNewCharacter(account, character)){
				//Character with this name already exists
				NetProtocol.srCharCreateStatus(this, 
									CharacterCreateStatus.INVALID_NAME);
				return;
			}
			//Character created succsessfully
			NetProtocol.srCharCreateStatus(this, CharacterCreateStatus.OK);
			cm.sendCharacterListToPlayer(this);
		}else{
			//Can not create more characters. Character limit reached
			NetProtocol.srCharCreateStatus(this, 
											CharacterCreateStatus.NOT_ALLOWED);
		}
	}
	
	/* Sends packet to this player */
	public void send(Object pack, boolean isReliable){
		if(client == null)
			return;
		if(isReliable){
			client.sendTCP(pack);
		}else{
			client.sendUDP(pack);
		}
	}
	
	/* Disconnects player from game */
	public void disconnect(){
		if(state == PlayerState.IN_GAME && character != null){
			//Let all near by players know that this player is left game
			for(int i = 0; i < entitiesInRange.size(); i++){
				Entity tmpE = entitiesInRange.get(i);
				if(!tmpE.isPlayer()){
					continue;
				}
				NetProtocol.srEntRemove(((Player) tmpE).getPlayerHandler(),
										character.getId());
				((Player) tmpE).getPlayerHandler()
										.removeEntityFromInRangeList(character);
			}
		}
		state = PlayerState.DISCONNECTED;
		disconnectTime = System.currentTimeMillis();
		entitiesInRange = null;
		actions = null;
		client.close();
		client = null;
	}	
	
	/* Calculate list of needed map chunks for this player 
	 * This function should only be called if player has moved to new chunk.
	 * ...that needs to be manualy checked */
	public void calcNeededChunks(){
		/* TODO: Ova funkcija moze MNOGO da se optimizira, tako sto nemoj koristiti
		 *  List<NeedMapChunk>, bespotrebno je. Nego napravi 2dArray neededChunks[3][3]*/
		
		MapManager tmpM = Common.getMapManagerSt();
		Point start = tmpM.pixelToChunk((int)character.getLocX(), 
										(int)character.getLocY());
		Point end = new Point(start);
		start.x -= 1;
		start.y -= 1;
		end.x += 1;
		end.y += 1;
		
		int worldWidth = tmpM.getWorldWidth();
		int worldHeight = tmpM.getWorldHeight();
		
		if(start.x < 0)
			start.x = 0;
		if(start.y < 0)
			start.y = 0;
		if(end.x >= worldWidth)
			end.x = worldWidth - 1;
		if(end.y >= worldHeight)
			end.y = worldHeight - 1;
		if(start.x >= end.x)
			start.x = end.x - 2;
		if(start.y >= end.y)
			start.y = end.y - 2;
		
		List<NeedMapChunk> tmpList = new ArrayList<NeedMapChunk>();
		
		for(int i = start.x; i <= end.x; i++){
			for(int j = start.y; j <= end.y; j++){
				NeedMapChunk tmpChunk = new NeedMapChunk(i, j);
				tmpList.add(tmpChunk);
			}
		}
		
		for(int i = 0; i < neededChunks.size(); i++){
			int index = tmpList.indexOf(neededChunks.get(i));
			if(index == -1){
				continue;
			}
			if(neededChunks.get(i).equals(tmpList.get(index))){
				if(neededChunks.get(i).isSend){
					tmpList.get(index).isSend = true;
				}
			}
		}
		newMapChunks = true;
		neededChunks = tmpList;
	}
	
	public long getDisconnectTime() {
		return disconnectTime;
	}

	public PlayerState getState(){
		return state;
	}
	
	public void setState(PlayerState _state){
		state = _state;
	}
	
	public int getConnectionId(){
		if(client == null)
			return -1;
		return client.getID();
	}
	
	/* Return connection(session) id */
	public int getSession(){
		return getConnectionId();
	}
	
	public boolean isConnected(){
		if(client == null)
			return false;
		return client.isConnected();
	}
	
	public Connection getConnection(){
		return client;
	}
	
	public Account getAccount(){
		return account;
	}

	public void setAccount(Account _account){
		account = _account;
	}
	
	public List<Entity> getEntitiesInRange() {
		return entitiesInRange;
	}

	public void setEntitiesInRange(List<Entity> entitiesInRange) {
		this.entitiesInRange = entitiesInRange;
	}
	
	/* Adds entity to list of visible entities*/
	public void addEntityToInRangeList(Entity e){
		entitiesInRange.add(e);
	}
	
	/* Remove entity from list of visible entities */
	public void removeEntityFromInRangeList(Entity e){
		entitiesInRange.remove(e);
	}
	
	/* Return whether is entity on this player's range list */
	public boolean isOnRangeList(Entity e){
		return entitiesInRange.contains(e);
	}
	
	/* Return InRange entity on 'i' this position on range list */
	public Entity getInRangeEntity(int i){
		return entitiesInRange.get(i);
	}

	public Player getCharacter(){
		return character;
	}
	
	public int getActionsCount(){
		return actions.size();
	}
	
	public PlayerAction getAction(){
		if(actions.size() == 0){
			return null;
		}
		PlayerAction tmpAction = actions.get(0);
		actions.remove(0);
		return tmpAction;
	}
	
	public void clearActions(){
		actions.clear();
	}
	
	public List<NeedMapChunk> getNeedChunks() {
		return neededChunks;
	}

	/* Add new chunk to neededChunks */
	public void addNeededChunk(int x, int y){
		neededChunks.add(new NeedMapChunk(x, y));
	}
	
	/* Return NeedMapChunk object 
	 * Returns null if not found */
	public NeedMapChunk getNeededChunk(int x, int y){
		for(int i = 0; i < neededChunks.size(); i++){
			NeedMapChunk tmpChunk = neededChunks.get(i);
			if(tmpChunk.x == x && tmpChunk.y == y){
				return tmpChunk;
			}
		}
		return null;
	}
	
	/* Removes this chunk from needed chunks list */
	public void removeNeededChunk(int x, int y){
		for(int i = 0; i < neededChunks.size(); i++){
			NeedMapChunk tmpChunk = neededChunks.get(i);
			if(tmpChunk.x == x && tmpChunk.y == y){
				neededChunks.remove(i);
				return;
			}
		}
	}
	
	/* Returns whether new map chunks needs to be send to player */
	public boolean isNewMapChunks(){
		return newMapChunks;
	}
	
	public void setNewMapChunks(boolean b){
		newMapChunks = b;
	}
	
	public boolean equals(Object obj){
		return getConnectionId() == ((PlayerHandler) obj).getConnectionId();
	}
}
