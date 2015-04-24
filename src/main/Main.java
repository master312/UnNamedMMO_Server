package main;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;

public class Main implements Runnable {

	/* Target logic ticking rate */
	private static final int TARGET_LOGIC = 30;
	private long lastFrame = 0;
	
	public Main() throws IOException, InterruptedException{
		Common.get();	//Initialize common class
		Common.getAccountsManagerSt().loadAccounts();
		Common.getCharactersManagerSt().loadCharacters();
		Common.getServerSt().startListening();
		
		
		//If multyple threads are needed, create them here
		Thread t = new Thread(this);
		t.start(); //Start logic thread
		
		startHookingConnections();
	}
	
	private void startHookingConnections() 
			throws InterruptedException, IOException{
		Connection tmpC = null;
		while(true){
			//Common.getServerSt().getSocket().update(0);
			
			tmpC = Common.getServerSt().getNewConnection();
			if(tmpC != null){
				//We have new connection
				Common.addPlayerToListSt(new PlayerHandler(tmpC));
				tmpC = null;
			}
			
			Common.getCharactersManagerSt().update();
			Common.clearTick(); //Move this from startHookingConnection; Maby new thread?
			Thread.sleep(25);
		}
	}
	
	public int getDelta() {
	    long time = System.currentTimeMillis();
	    int delta = (int) (time - lastFrame);
	    lastFrame = time;
 
	    return delta;
	}
	@Override
	public void run() {	
		getDelta();
		while(true){
			int delta = getDelta();
			
			logicTick(delta);
			
			if(delta < 1000 / TARGET_LOGIC){
				try {
					Thread.sleep((1000 / TARGET_LOGIC) - delta);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void logicTick(int deltaTime){
		Common.getGameManagerSt().update(deltaTime);
	}
	
	public static void main(String[] args) 
			throws IOException, InterruptedException {
		new Main();
	}
}

//class TmpListener extends Listener{
//	
//	public TmpListener(){
//	}
//	public void received(Connection c, Object p){
//		if(p instanceof Packet){
//			Packet tmpP = (Packet)p;
//			if(tmpP.readInt() == 666){
//				System.out.println(c.getID() + " Moved: " + tmpP.readInt() + " " + tmpP.readInt());
//			}
//		}
//	}
//}