package net;

import java.io.IOException;
import java.util.ArrayList;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import entities.Entity;
import entities.Pawn;
import entities.Player;

/* TODO: client.update(1) function maby... */
public class ClientSocket extends Listener{
	/* How long to wait, before connection fails */
	private static final int CONNECTION_TIMEOUT = 5000;
	
	static Client client = null;
	/* List of received packets */
	private ArrayList<Object> packets = new ArrayList<Object>();
	
	public ClientSocket(){
		client = new Client();
	}
	
	public void connect(String ip, int tcpPort, int udpPort) throws IOException{
		client.getKryo().register(byte[].class);
		client.getKryo().register(Packet.class);
		client.getKryo().register(Entity.class);
		client.getKryo().register(Pawn.class);
		client.getKryo().register(Player.class);
		client.getKryo().register(entities.Entity.EntityType.class);
		client.getKryo().register(entities.Entity.Direction.class);
		
		client.setTimeout(CONNECTION_TIMEOUT);

		client.start();
		client.connect(CONNECTION_TIMEOUT, ip, tcpPort, udpPort);
		client.setKeepAliveTCP((CONNECTION_TIMEOUT / 2) + 1);
		
		client.addListener(this);
	}
	
	public void received(Connection c, Object p){
		if(p instanceof Packet || p instanceof Entity
				|| p instanceof Pawn || p instanceof Player){
			packets.add(p);
		}
	}
	
	public void disconnected(Connection con){
	}
	
	/* Return received packet, or null if there is not any */
	public Packet getPacket(){
		for(int i = 0; i < packets.size(); i++){
			if(packets.get(i) instanceof Packet){
				Packet tmpPack = (Packet) packets.get(i);
				tmpPack.resetPointer();
				packets.remove(i);
				return tmpPack;
			}
		}
		return null;
	}
	
	/* Return packet converted to entity object */
	public Entity getEntity(){
		for(int i = 0; i < packets.size(); i++){
			if(packets.get(i) instanceof Entity){
				Entity tmpPack = (Entity) packets.get(i);
				packets.remove(i);
				return tmpPack;
			}
		}
		return null;
	}
	
	public void send(Object pack, boolean isReliable){
		if(isReliable){
			client.sendTCP(pack);
		}else{
			client.sendUDP(pack);
		}
	}
	
	public void disconnect(){
		clearPackets();
		client.close();
	}
	
	/* Clear packets query */
	public void clearPackets(){
		packets.clear();
	}
	
	public boolean isConnected(){
		return client.isConnected();
	}
}
