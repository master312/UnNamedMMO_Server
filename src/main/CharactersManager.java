package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.NetProtocol;

import com.esotericsoftware.minlog.Log;

import entities.Player;
import main.AccountsManager.Account;
import main.PlayerHandler.PlayerState;

/* This class handles loading/unloading player characters
 * It also handles clients that are currently on login screen state */

public class CharactersManager {
	class AccountCharacters{
		/* This class holds all characters from same account */
		private int accountId = -1;
		private List<Player> characters = new ArrayList<Player>();
		
		public AccountCharacters(int _accountId){
			accountId = _accountId;
		}

		/* Return number of characters loaded */
		public int loadCharacters(){
			//TODO THis!: Here!
			return 0;
		}
		
		/* Return list of characters */
		public List<Player> getCharacters(){
			return characters;
		}
		
		public int getAccountId(){
			return accountId;
		}
		
		public int getCharactersCount(){
			return characters.size();
		}
		
		/* Return character with charId, or null if none is found */
		public Player getCharacter(int charId){
			for(int i = 0; i < characters.size(); i++){
				if(characters.get(i).getId() == charId)
					return characters.get(i);
			}
			return null;
		}
		
		public boolean equals(Object obj){
			if(obj instanceof Account){
				return accountId == ((Account) obj).getId();
			}else if(obj instanceof AccountCharacters){
				return accountId == ((AccountCharacters) obj).getAccountId();
			}
			return false;
		}
	}
	
	/* Interval on what will characters be send to players
	 * that are on characters screen (ms)*/
	private static final int UPDATE_INTERVAL = 1000;
	private static long lastUpdate = 0;
	
	/* List of all characters on server */
	private List<AccountCharacters> characters =
			Collections.synchronizedList(new ArrayList<AccountCharacters>());
	/* Total number of characters loaded */
	private int charCount = 0;
	
	public CharactersManager(){
	}
	
	/* Load all characters */
	public void loadCharacters(){
		List<Account> accounts = Common.getAccountsManagerSt().getAccounts();
		characters.clear();
		charCount = 0;
		for(int i = 0; i < accounts.size(); i++){
			characters.add(loadAccountCharacters(accounts.get(i)));
		}
		Log.debug("Loaded " + charCount + " characters");
	}
	
	/* Load all characters for this account 
	 * Return account characters object*/
	public AccountCharacters loadAccountCharacters(Account account){
		AccountCharacters ac = new AccountCharacters(account.getId());
		int tmpCount = ac.loadCharacters();
		charCount += tmpCount;
		return ac;
	}
	
	/* Update function. Called on interval
	 * Send characters to client (on login screen) */
	public void update(){
		if(System.currentTimeMillis() - lastUpdate < UPDATE_INTERVAL){
			return;
		}
		lastUpdate = System.currentTimeMillis();
		
		/* List of players that are not in game */
		List<PlayerHandler> players = Common.getPreGamePlayersSt();
		for(int i = 0; i < players.size(); i++){
			PlayerHandler tmpP = players.get(i);
			if(tmpP.getState() == PlayerState.LOGGED_IN){
				//Player just logged in, and needs to receive character list
				sendCharacterListToPlayer(tmpP);
				Common.getPreGamePlayersSt()  //Changes player state to character screen
						.get(i)
						.setState(PlayerState.CHARACTER_SCREEN);
			}
		}
	}
	
	public void sendCharacterListToPlayer(PlayerHandler player){
		/*Player is logged in, and waiting for characters
		 *so that it can switch to characters screen */
		AccountCharacters tChars = getAccCharacters(player.getAccount());
		if(tChars == null)	//Fail safe
			return;
		List<Player> chars = tChars.getCharacters();
		NetProtocol.srCharCount(player, (short)chars.size());
		for(int j = 0; j < chars.size(); j++){
			//Sending player entity (character) object to client
			NetProtocol.srEntPlayer(player, chars.get(j));
		}
	}
	
	/* Return AccountCharacters object for acc.
	 * Return null if not found */
	public AccountCharacters getAccCharacters(Account acc){
		for(int i = 0; i < characters.size(); i++){
			if(characters.get(i).getAccountId() == acc.getId()){
				return characters.get(i);
			}
		}
		return null;
	}
	
	/* Returns false if character with same name already exists 
	 * Returns true if character was created successfully */
	public boolean createNewCharacter(Account acc, Player character){
		int accIndex = 0;
		int newCharId = 0;
		for(int i = 0; i < characters.size(); i++){
			if(acc.getId() == characters.get(i).getAccountId()){
				accIndex = i;
			}
			List<Player> chars = characters.get(i).getCharacters();
			for(int j = 0; j < chars.size(); j++){
				if(chars.get(j).getName().equals(character.getName())){
					//Character with same name already exists
					return false;
				}
				if(chars.get(j).getId() > newCharId)
					newCharId = chars.get(j).getId();
			}
		}
		newCharId += 1;
		//TODO: Set character's default values here
		character.setId(newCharId);
		characters.get(accIndex).getCharacters().add(character);
		charCount++;
		Log.info("New character '" + character.getName() + "' created");
		return true;
	}

	/* Returns character, or null if not found */
	public Player getCharacter(Account acc, int charId){
		AccountCharacters chars = getAccCharacters(acc);
		if(chars == null)
			return null;
		return chars.getCharacter(charId);
	}
}
