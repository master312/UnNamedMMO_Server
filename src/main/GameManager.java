package main;

import java.util.List;

import entities.Player;
import net.NetProtocol;

public class GameManager {
	private List<PlayerHandler> players;
	private int delta = 0;
	
	public GameManager(){
	}
	
	/* Initialize GameManager class */
	public void initialize(){
		players = Common.getInGamePlayersSt();
	}
	
	/* Update game logic */
	public void update(int _delta){
		delta = _delta;
		for(int i = 0; i < players.size(); i++){
			if(!players.get(i).isConnected())
				continue;
			handlePlayer(players.get(i));
		}
	}
	
	/* Update visibility lists */
	public void visibilityUpdate(){
		for(int i = 0; i < players.size(); i++){
			PlayerHandler tmpPl = players.get(i);
			if(!tmpPl.isConnected())
				continue;
			updateVisibilityList(tmpPl);
			if(tmpPl.isNewMapChunks()){
				//Sending new map chunks to player
				Common.getMapManagerSt().sendChunks(tmpPl);
				tmpPl.setNewMapChunks(false);
			}
		}
	}
	
	/* Handle player actions */
	private void handlePlayer(PlayerHandler pl){
		PlayerActionHandler.handlePlayerActions(pl, delta);
	}
	
	/* Update visibility list for pl */
	private void updateVisibilityList(PlayerHandler pl){
		for(int i = 0; i < players.size(); i++){
			PlayerHandler tmpPlayer = players.get(i);
			
			if(tmpPlayer.getSession() == pl.getSession() || 
					!tmpPlayer.isConnected()){
				continue;
			}
			
			/* Is this current player visible to player we are making list for */
			boolean isVisible = pl.isOnRangeList(tmpPlayer.getCharacter());
			Player tmpChar = tmpPlayer.getCharacter();
			if(GameMath.isInVisibleRange(pl.getCharacter(), tmpChar)){
				if(isVisible)
					continue;
				pl.addEntityToInRangeList(tmpChar);
				NetProtocol.srVisibleEntity(pl, tmpChar);
			}else{
				if(isVisible){
					/* Player is not in range, but its visible. 
					 * So we make him invisible :) */
					NetProtocol.srEntRemove(pl, tmpChar.getId());
					pl.removeEntityFromInRangeList(tmpChar);
				}
			}
		}
	}
}
