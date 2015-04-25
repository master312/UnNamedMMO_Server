package main;

import java.util.List;

import entities.Player;
import net.NetProtocol;

public class GameManager {
	private List<PlayerHandler> players;
	private int delta = 0;
	
	public GameManager(){
	}
	
	public void initialize(){
		players = Common.getInGamePlayersSt();
	}
	
	public void update(int _delta){
		delta = _delta;
		for(int i = 0; i < players.size(); i++){
			if(!players.get(i).isConnected())
				continue;
			handlePlayer(players.get(i));
		}
	}
	
	public void listUpdate(){
		for(int i = 0; i < players.size(); i++){
			if(!players.get(i).isConnected())
				continue;
			updateVisibilityList(players.get(i));
		}
	}
	
	private void handlePlayer(PlayerHandler pl){
		PlayerActionHandler.handlePlayerActions(pl, delta);
	}
	
	private void updateVisibilityList(PlayerHandler pl){
		for(int i = 0; i < players.size(); i++){
			PlayerHandler tmpPlayer = players.get(i);
			if(tmpPlayer.getSession() == pl.getSession() || 
					!tmpPlayer.isConnected()) 
				continue;	//We don't want to sent player to him self
			/* Is this current visible to player we are making list for*/
			boolean isVisible = pl.isVisible(tmpPlayer.getCharacter());
			Player tmpChar = tmpPlayer.getCharacter();
			if(Common.isInRange(tmpPlayer.getCharacter(), tmpChar)){
				if(isVisible)
					continue;
				pl.addEntityToList(tmpChar);
				NetProtocol.srVisibleEntity(pl, tmpChar);
			}else{
				if(isVisible){
					//Player is not in range, but its visible
					NetProtocol.srEntRemove(pl, tmpChar.getId());
					pl.removeEntityFromList(tmpChar);
				}
			}
		}
	}
}
