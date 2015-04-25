package main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import net.NetProtocol;
import net.NetProtocol.LoginStatus;

import com.esotericsoftware.minlog.Log;

/* This class handles client logins */
public class AccountsManager {
	public enum AccessLevel{
		PLAYER, GAME_MASTER, ADMIN, GOD
	}
	
	public class Account{
		private int id;
		private String username;
		private String password;
		private AccessLevel accessLevel;
		private int banned;
		
		public Account(int _id, String _username, String _password,
				AccessLevel _accessLevel, int _banned){
			id = _id;
			username = _username;
			password = _password;
			accessLevel = _accessLevel;
			banned = _banned;
		}
		
		public Account(){
			id = -1;
			username = "";
			password = "";
			accessLevel = AccessLevel.PLAYER;
			banned = -1;
		}
		
		public boolean equals(Object obj){
			return username.equals(((Account) obj).getUsername()) &&
					password.equals(((Account) obj).getPassword());
		}
		
		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public AccessLevel getAccessLevel() {
			return accessLevel;
		}

		public void setAccessLevel(AccessLevel accessLevel) {
			this.accessLevel = accessLevel;
		}

		public int getBanned() {
			return banned;
		}

		public void setBanned(int banned) {
			this.banned = banned;
		}
	}
	
	/* List containing all accounts */
	private List<Account> accounts = 
			Collections.synchronizedList(new ArrayList<Account>());
	
	public AccountsManager(){
	}
	
	/* Load account list from database */
	public void loadAccounts(){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("accounts"));
			String line = br.readLine();
			while(line != null){
				loadAccount(line);
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		Log.info("Loaded " + accounts.size() + " accounts");
	}
	
	/* Create account object from string line */
	private void loadAccount(String line){
		String elements[] = line.split(" ");
		AccessLevel tmpLevel = null;
		switch(Integer.parseInt(elements[2])){
		case 1: tmpLevel = AccessLevel.PLAYER; break;
		case 2: tmpLevel = AccessLevel.GAME_MASTER; break;
		case 3: tmpLevel = AccessLevel.ADMIN; break;
		case 4: tmpLevel = AccessLevel.GOD; break;
		}
		Account tmpAcc = new Account(accounts.size() + 1, elements[0], 
									elements[1], 
									tmpLevel, 
									Integer.parseInt(elements[3]));
		accounts.add(tmpAcc);
	}
	
	/* Handles player login.
	 * This function also sends login status to socket
	 * Returns true if login is OK, or -1 if fail */
	public boolean handleLogin(PlayerHandler pl){
		int tmpIndex = accounts.indexOf(pl.getAccount());
		if(tmpIndex > -1){
			pl.setAccount(accounts.get(tmpIndex));
			NetProtocol.srLoginStatus(pl, LoginStatus.LOGIN_OK);
			return true;
		}
		NetProtocol.srLoginStatus(pl, LoginStatus.LOGIN_FAIL);
		return false;
	}
	
	/* Return list of accounts */
	public List<Account> getAccounts(){
		return accounts;
	}
}
