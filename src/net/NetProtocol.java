package net;

import entities.Entity;
import entities.Pawn;
import entities.Player;
import main.Common;
import main.PlayerHandler;
import main.PlayerHandler.CharacterCreateStatus;
import map.MapChunk;

/* This class package network packets */
public class NetProtocol {
	public enum LoginStatus{
		LOGIN_OK, LOGIN_FAIL
	}
	
	//TODO: This class probably should not be defined here
	class EntityUpdates{
		public static final short POSITION = 1;
		public static final short DIRECTION = 2;
		public static final short POS_DIR = 3;	//Position and direction
	}
	
	/* Sends server ready packet to client */
	public static void srLoginReady(PlayerHandler cl){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_LOGIN_READY);
		cl.send(pb.getPacket(), true);
	}
	
	/* Sends login status to client */
	public static void srLoginStatus(PlayerHandler cl, LoginStatus st){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_LOGIN_STATUS);
		if(st == LoginStatus.LOGIN_OK){
			pb.writeShort((short)1);
		}else{
			pb.writeShort((short)2);
		}
		cl.send(pb.getPacket(), true);
	}
	
	public static void srCharCount(PlayerHandler cl, short count){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_CHAR_COUNT);
		pb.writeShort(count);
		cl.send(pb.getPacket(), true);
	}
	
	/* Sends 'player entity' to client */
	public static void srEntPlayer(PlayerHandler cl, Player pl){
		cl.send(pl, true);
	}
	
	/* Sends pawn entity to client */
	public static void srEntPawn(PlayerHandler cl, Pawn pw){
		cl.send(pw, true);
	}
	
	/* Sends status for newly created character */
	public static void srCharCreateStatus(PlayerHandler cl, 
			CharacterCreateStatus status){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_CHAR_CREATE_STATUS);
		switch(status){
		case OK:
			pb.writeShort((short)1);
			break;
		case INVALID_NAME:
			pb.writeShort((short)2);
			break;
		default:
			pb.writeShort((short)3);
			break;
		}
		cl.send(pb.getPacket(), true);
	}
	
	/* Sends whole visible entity list to client */
	public static void srVisibleEntList(PlayerHandler cl){
		for(int i = 0; i < cl.getEntitiesInRange().size(); i++){
			cl.send(cl.getInRangeEntity(i), true);
		}
	}

	/* Sends new entity to player */
	public static void srVisibleEntity(PlayerHandler cl, Entity e){
		cl.send(e, true);
	}
	
	/* Tells client to remove entity */
	public static void srEntRemove(PlayerHandler cl, int entityId){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_ENT_REMOVE);
		pb.writeInt(entityId);
		cl.send(pb.getPacket(), true);
	}
	
	/* Sends pawn position update to client */
	public static void srPawnUpdatePosition(PlayerHandler cl, Pawn pawn){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_PAWN_UPDATE);
		pb.writeInt(pawn.getId());
		pb.writeShort(EntityUpdates.POSITION);
		pb.writeInt((int)pawn.getLocX());
		pb.writeInt((int)pawn.getLocY());
		cl.send(pb.getPacket(), false);
	}
	
	/* Sends pawn direction update to client */
	public static void srPawnUpdateDirection(PlayerHandler cl, Pawn pawn){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_PAWN_UPDATE);
		pb.writeInt(pawn.getId());
		pb.writeShort(EntityUpdates.DIRECTION);
		pb.writeShort(pawn.getNetDir());
		cl.send(pb.getPacket(), false);
	}
	
	/* Sends pawn direction update to client */
	public static void srPawnUpdatePosDir(PlayerHandler cl, Pawn pawn){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_PAWN_UPDATE);
		pb.writeInt(pawn.getId());
		pb.writeShort(EntityUpdates.POS_DIR);
		pb.writeInt((int)pawn.getLocX());
		pb.writeInt((int)pawn.getLocY());
		pb.writeShort(pawn.getNetDir());
		cl.send(pb.getPacket(), false);
	}
	
	/* Sends map chunk to player */
	public static void srMapChunk(PlayerHandler cl, MapChunk mc){
		cl.send(mc, true);
	}
	
	/* Sends world size to client */
	public static void srWorldSize(PlayerHandler cl){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_WORLD_SIZE);
		pb.writeInt(Common.getMapManagerSt().getWorldWidth());
		pb.writeInt(Common.getMapManagerSt().getWorldHeight());
		pb.writeInt(Common.getMapManagerSt().getChunkWidth());
		pb.writeInt(Common.getMapManagerSt().getChunkHeight());
		cl.send(pb.getPacket(), true);
	}
	
	/* Sends text message to client */
	public static void srTextMsg(PlayerHandler cl, short type, String sender, 
									String msg){
		PacketBuilder pb = new PacketBuilder();
		pb.writeShort(OpCodes.SR_TEXT_MSG);
		pb.writeShort(type);
		pb.writeString(sender);
		pb.writeString(msg);
		cl.send(pb.getPacket(), true);
	}
}
