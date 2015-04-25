package main;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;

public class Main {

	/* Target logic ticking rate */
	private static final int TARGET_LOGIC = 30;
	private long lastFrame = 0;
	
	public Main() throws IOException, InterruptedException{
		Common.get();	//Initialize common class
		Common.getGameManagerSt().initialize();
		Common.getAccountsManagerSt().loadAccounts();
		Common.getCharactersManagerSt().loadCharacters();
		Common.getServerSt().startListening();
		
		
		
//		Thread t = new Thread(this);
//		t.start(); //Start logic thread
		initThreads();
		
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
		//This is game logic thread
		getDelta();
		while(true){
			int delta = getDelta();
			
			Common.getGameManagerSt().update(delta);
			
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
	
	public void threadTwo(){
		while(true){
			/* This thread updates players visibility lists */
			Common.getGameManagerSt().listUpdate();
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) 
			throws IOException, InterruptedException {
		new Main();
	}
}