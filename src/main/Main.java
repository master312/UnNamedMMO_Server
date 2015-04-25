package main;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

public class Main {

	/* Target logic ticking rate */
	private static final int TARGET_LOGIC = 33;
	private long lastTick = 0;
	
	public Main() throws IOException, InterruptedException{
		Common.get();								//Initialize common class
		Common.getGameManagerSt().initialize();		//Initialize game manager
		Common.getAccountsManagerSt().loadAccounts();//Load accounts
		Common.getCharactersManagerSt().loadCharacters();//Load characters
		Common.getServerSt().startListening();		//Open listening socket
		
		initThreads();
		
		Log.info("Server ready!");
		
		while(true){
			hookConnections();
			Common.getCharactersManagerSt().update();
			threadSleep(5);
		}	
	}
	
	private void hookConnections(){
		Connection tmpC = Common.getServerSt().getNewConnection();
		if(tmpC != null){
			//We have new connection
			Common.addPlayerToListSt(new PlayerHandler(tmpC));
		}
	}
	
	public int getDelta() {
	    long time = System.currentTimeMillis();
	    int delta = (int) (time - lastTick);
	    lastTick = time;
	    return delta;
	}
	
	public void initThreads(){
		//If multiple threads are needed later, create them here
		Thread t = new Thread(new Runnable(){
			public void run(){
				threadOne();
			}
		});
		t.start();
		
		Thread t2 = new Thread(new Runnable(){
			public void run(){
				threadTwo();
			}
		});
		t2.start();
	}
	
	public void threadOne(){
		/* This is game logic thread */
		getDelta();
		while(true){
			int delta = getDelta();
			if(delta < 1000 / TARGET_LOGIC){
				threadSleep((1000 / TARGET_LOGIC) - delta);
				delta = getDelta();
			}
			Common.getGameManagerSt().update(delta);
		}
	}
	
	public void threadTwo(){
		/* This thread updates players visibility lists 
		 * Clears inactive player objects */
		while(true){
			Common.clearTick();
			Common.getGameManagerSt().visibilityUpdate();
			threadSleep(200);
		}
	}
	
	private void threadSleep(int ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
			throws IOException, InterruptedException {
		new Main();
	}
}