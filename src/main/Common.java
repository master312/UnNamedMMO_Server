package main;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import main.PlayerHandler.PlayerState;
import net.ServerSocket;

/* This class contains EVERYTHINH AHAHAHAHA !!! >:} */
public class Common {
	private static Common commonClass = null;
	
	/* Ports on what to listen for connections */
	private static final int LISTEN_PORT_TCP = 1234;
	private static final int LISTEN_PORT_UDP = 1235;
	/* Maximum number of players connected */
	private static final int MAXIMUM_CONNECTED = 100;
	/* Interval of clear tick */
	private static final int CLEAR_INTERVAL = 2000;
	private static long lastClear = 0;
	/* Player object timeout, after disconnection */
	private static final int PLAYER_OBJECT_TIMEOUT = 5000;
	/* Player connection timeout */
	public static final int CONNECTION_TIMEOUT = 5000;
	/* Maximum number of character pre account */
	public static final int MAXIMUM_ACC_CHAR_NUMBER = 5;
	
	private ServerSocket serverSocket = null;
	private AccountsManager accountManager = null;
	private GameManager gameManager = null;
	private CharactersManager charactersManager = null;
	
	/* List players that not logged in, or on character selection screen */
	private List<PlayerHandler> preGamePlayers =
			Collections.synchronizedList(new ArrayList<PlayerHandler>());
	/* List of players that are currently in game */
	private List<PlayerHandler> inGamePlayers = 
			Collections.synchronizedList(new ArrayList<PlayerHandler>());
	
	
	public Common(){
		serverSocket = new ServerSocket(LISTEN_PORT_TCP, 
				LISTEN_PORT_UDP, MAXIMUM_CONNECTED);
		accountManager = new AccountsManager();
		gameManager = new GameManager();
		charactersManager = new CharactersManager();
	}
	
	public ServerSocket getServer(){
		return serverSocket;
	}
	
	public static ServerSocket getServerSt(){
		return commonClass.getServer();
	}
	
	public AccountsManager getAccountsManager() {
		return accountManager;
	}

	public static AccountsManager getAccountsManagerSt() {
		return commonClass.getAccountsManager();
	}
	
	public GameManager getGameManager() {
		return gameManager;
	}
	
	public static GameManager getGameManagerSt() {
		return commonClass.getGameManager();
	}
	
	public CharactersManager getCharactersManager() {
		return charactersManager;
	}

	public static CharactersManager getCharactersManagerSt(){
		return commonClass.getCharactersManager();
	}
	
	/* Returns array list of players that are not yet in game
	 * On character select screen, or connected but not logged in yet */
	public List<PlayerHandler> getPreGamePlayers(){
		return preGamePlayers;
	}
	
	/* Returns array list of players that are not yet in game
	 * On character select screen, or connected but not logged in yet */
	public static List<PlayerHandler> getPreGamePlayersSt(){
		return commonClass.getPreGamePlayers();
	}
	
	/* Returns array list of players that are in game */
	public List<PlayerHandler> getInGamePlayers(){
		return inGamePlayers;
	}
	
	/* Returns array list of players that are in game */
	public static List<PlayerHandler> getInGamePlayersSt(){
		return commonClass.getInGamePlayers();
	}
	
	/* Adds new player to players array list */
	public void addPlayerToList(PlayerHandler player){
		if(player.getState() == PlayerState.IN_GAME){
			inGamePlayers.add(player);
		}else{
			//Player is connected, but not in game yet
			preGamePlayers.add(player);
		}
	}
	
	/* Adds new player to players array list */
	public static void addPlayerToListSt(PlayerHandler player){
		commonClass.addPlayerToList(player);
	}
	
	/* Removes player from array list 
	 * WARNING: Player MUST be on the list. 
	 * If not this function may crush program*/
	public void removePlayer(PlayerHandler player){
		if(preGamePlayers.contains(player)){
			preGamePlayers.remove(player);
		}else{
			inGamePlayers.remove(player);
		}
	}
	
	public static void removePlayerSt(PlayerHandler player){
		commonClass.removePlayer(player);
	}
	
	/* Called on interval. This funciton clear all inactive player objects */
	public static void clearTick(){
		if(System.currentTimeMillis() - lastClear < CLEAR_INTERVAL){
			return;
		}
		lastClear = System.currentTimeMillis();
		clearTickPreGame();
		clearTickInGame();
	}
	
	private static void clearTickPreGame(){
		for(int i = 0; i < getPreGamePlayersSt().size(); i++){
			PlayerHandler tmpP = getPreGamePlayersSt().get(i);
			if(tmpP.getState() == PlayerState.DISCONNECTED){
				if(lastClear - tmpP.getDisconnectTime() > PLAYER_OBJECT_TIMEOUT){
					if(tmpP.getConnection() != null)
						tmpP.getConnection().close();
					removePlayerSt(tmpP);
				}
			}
		}
	}

	private static void clearTickInGame(){
		for(int i = 0; i < getInGamePlayersSt().size(); i++){
			PlayerHandler tmpP = getInGamePlayersSt().get(i);
			if(tmpP.getState() == PlayerState.DISCONNECTED){
				if(lastClear - tmpP.getDisconnectTime() > PLAYER_OBJECT_TIMEOUT){
					if(tmpP.getConnection() != null)
						tmpP.getConnection().close();
					removePlayerSt(tmpP);
				}
			}
		}
	}
	
	public static Common get(){
		if(commonClass == null){
			commonClass = new Common();
		}
		return commonClass;
	}
}
