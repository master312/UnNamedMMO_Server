package main;

import main.AccountsManager.Account;
import main.CharactersManager.AccountCharacters;
import net.NetProtocol;
import net.OpCodes;
import net.Packet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

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
	
	/* Player network client object */
	private Connection client = null;
	private PlayerState state = null;
	/* Time when this player was disconnected (if disconnected) */
	private long disconnectTime = 0;
	/* Players user name and password */
	private Account account = null;
	
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
				handlePreGameData(tmpP);
			}else if(state == PlayerState.IN_GAME){
				
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

	/* Handle packets used before player switchs to in game state*/
	public void handlePreGameData(Packet pack){
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
			break;
		}
	}
	
	/* Handle creation of new character */
	public void handleNewCharacter(Player character){
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
	
	/* Sends packet to this player */
	public void send(Object pack, boolean isReliable){
		if(isReliable){
			client.sendTCP(pack);
		}else{
			client.sendUDP(pack);
		}
	}
	
	public void disconnect(){
		state = PlayerState.DISCONNECTED;
		disconnectTime = System.currentTimeMillis();
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
		return client.getID();
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
	
	public boolean equals(Object obj){
		return getConnectionId() == ((PlayerHandler) obj).getConnectionId();
	}
}
