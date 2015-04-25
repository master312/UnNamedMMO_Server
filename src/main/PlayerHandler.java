package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.AccountsManager.Account;
import main.CharactersManager.AccountCharacters;
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
 * on its creatino. Dont forget to delete it*/
public class PlayerHandler extends Listener{
	public enum PlayerState{
		CONNECTED, LOGGED_IN, CHARACTER_SCREEN, IN_GAME, DISCONNECTED
	}
	
	public enum CharacterCreateStatus{
		OK, INVALID_NAME, NOT_ALLOWED
	}
	
	public enum PlayerActionType{
		NOTHING, MOVE
	}
	
	public class PlayerAction{
		public PlayerActionType type = PlayerActionType.NOTHING;
		public List<Object> values = new ArrayList<Object>();
		
		public PlayerAction(PlayerActionType _type){
			type = _type;
		}
	}
	
	/* Direction ID's received over network */
	private static final int DIRECTION_NORT = 0;
	private static final int DIRECTION_NORTHEAST = 1;
	private static final int DIRECTION_EAST = 2;
	private static final int DIRECTION_SOUTHEAST = 3;
	private static final int DIRECTION_SOUTH = 4;
	private static final int DIRECTION_SOUTHWEST = 5;
	private static final int DIRECTION_WEST = 6;
	private static final int DIRECTION_NORTHWEST = 7;
	
	/* Player network client object */
	private Connection client = null;
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
	
	public PlayerHandler(Connection cl){
		client = cl;
		client.setTimeout(Common.CONNECTION_TIMEOUT);
		client.addListener(this);
		client.setKeepAliveTCP(Common.CONNECTION_TIMEOUT / 2);
		state = PlayerState.CONNECTED;
		
		NetProtocol.srLoginReady(this);	//Sends ready signal to client
	}
	
	/* Called when when packet is received */
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
				return;
			}//Characters can ONLY be created on character screen state
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
				Log.info("Player entered game with char " + character.getName());
			}
			break;
		}
	}
	
	/* Handle packets used inGame */
	public void handleInGameNetwork(Packet pack){
		short opCode = pack.readShort();
		switch(opCode){
		case OpCodes.CL_MOVE:
			handleMovementPacket(pack);
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
				NetProtocol.srCharCreateStatus(this, 
										CharacterCreateStatus.INVALID_NAME);
				return;
			}
			NetProtocol.srCharCreateStatus(this, CharacterCreateStatus.OK);
			cm.sendCharacterListToPlayer(this);
		}else{
			//Can not create more characters. Character limit reached
			NetProtocol.srCharCreateStatus(this, 
											CharacterCreateStatus.NOT_ALLOWED);
		}
	}
	
	private void handleMovementPacket(Packet pack){
		PlayerAction tmpAction = new PlayerAction(PlayerActionType.MOVE);
		switch(pack.readShort()){
		//Switch direction
		case DIRECTION_NORT:
			tmpAction.values.add(Entity.Direction.NORTH);
			break;
		case DIRECTION_NORTHEAST:
			tmpAction.values.add(Entity.Direction.NORTHEAST);
			break;
		case DIRECTION_EAST:
			tmpAction.values.add(Entity.Direction.EAST);
			break;
		case DIRECTION_SOUTHEAST:
			tmpAction.values.add(Entity.Direction.SOUTHEAST);
			break;
		case DIRECTION_SOUTH:
			tmpAction.values.add(Entity.Direction.SOUTH);
			break;
		case DIRECTION_SOUTHWEST:
			tmpAction.values.add(Entity.Direction.SOUTHWEST);
			break;
		case DIRECTION_WEST:
			tmpAction.values.add(Entity.Direction.WEST);
			break;
		case DIRECTION_NORTHWEST:
			tmpAction.values.add(Entity.Direction.NORTHWEST);
			break;
		}
		actions.add(tmpAction);
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
				((Player) tmpE).getPlayerHandler().removeEntityFromList(character);
			}
		}
		state = PlayerState.DISCONNECTED;
		disconnectTime = System.currentTimeMillis();
		entitiesInRange = null;
		actions = null;
		client.close();
		client = null;
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
	public void addEntityToList(Entity e){
		entitiesInRange.add(e);
	}
	
	/* Remove entity from list of visible entities */
	public void removeEntityFromList(Entity e){
		entitiesInRange.remove(e);
	}
	
	/* Return whether is entity on this player's visible list */
	public boolean isVisible(Entity e){
		return entitiesInRange.contains(e);
	}
	
	/* Return referenve to visible entity on this position in list */
	public Entity getVisibleEntity(int i){
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
	
	public boolean equals(Object obj){
		return getConnectionId() == ((PlayerHandler) obj).getConnectionId();
	}
}
