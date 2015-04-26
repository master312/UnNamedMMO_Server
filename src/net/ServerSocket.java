package net;

import java.io.IOException;
import java.util.ArrayList;

import map.MapChunk;
import map.Tile;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import entities.Entity;
import entities.Pawn;
import entities.Player;

public class ServerSocket extends Listener{
	/* Kryonet server object */
	private static Server server = null;
	
	/* TCP and UDP listening ports */
	private int tcpPort;
	private int udpPort;
	
	/* Maximum number of connections */
	private int maxConnections;
	
	/* List of all clients connected, and handled by program */
	private ArrayList<Connection> clients = new ArrayList<Connection>();
	private ArrayList<Connection> unHandledClients = new ArrayList<Connection>();
	
	public ServerSocket(int _tcpPort, int _udpPort, int _maxConnected){
		tcpPort = _tcpPort;
		udpPort = _udpPort;
		maxConnections = _maxConnected;
		server = new Server(64000, 64000);
	}
	
	/* Start listening for clients */
	public void startListening() throws IOException{
		//Register packet classes
		server.getKryo().register(byte[].class);
		server.getKryo().register(Packet.class);
		server.getKryo().register(Entity.class);
		server.getKryo().register(Pawn.class);
		server.getKryo().register(Player.class);
		server.getKryo().register(entities.Entity.EntityType.class);
		server.getKryo().register(entities.Entity.Direction.class);
		
		server.getKryo().register(MapChunk.class);
		server.getKryo().register(MapChunk.Layer.class);
		server.getKryo().register(Tile.class);
		server.getKryo().register(Tile[].class);
		server.getKryo().register(Tile[][].class);
		server.getKryo().register(short[].class);
		
		server.bind(tcpPort, udpPort);
		server.start();
		server.addListener(this);
		//Log.DEBUG("Server started..."); //Not needed. Kryonet have its own loging system
	}
	
	/* This is run when new client is connected */
	public void connected(Connection c){
		if(getConnectedCount() >= maxConnections){
			c.close();
			Log.info("Client kicked. Server is full");
			return;
		}
		unHandledClients.add(c);
	}
	
	/* This is run when a client has disconnected. */
	public void disconnected(Connection c){
		if(unHandledClients.contains(c)){
			unHandledClients.remove(c);
		}else if(clients.contains(c)){
			clients.remove(c);
		}
	}
	
	/* Return connection if there was new client connected, or NULL if none */
	public Connection getNewConnection(){
		int tmpSize = unHandledClients.size();
		if(tmpSize == 0){
			return null;
		}else{
			Connection tmpC = unHandledClients.get(tmpSize - 1);
			unHandledClients.remove(tmpSize - 1);
			return tmpC;
		}	
	}
	
	/* Return number of connected clients */
	public int getConnectedCount(){
		return clients.size() + unHandledClients.size();
	}
	
	public Server getSocket(){
		return server;
	}
}
