package main;

import java.util.ArrayList;
import java.util.List;

public class PlayerAction {
	
	public enum PlayerActionType{
		NOTHING, MOVE, CHAT
	}
	
	public PlayerActionType type;
	public List<Object> values = new ArrayList<Object>();
	
	public PlayerAction(PlayerActionType _type){
		type = _type;
	}
	
	public PlayerAction(){
		type = PlayerActionType.NOTHING;
	}
}
