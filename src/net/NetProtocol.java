package net;

import entities.Pawn;
import entities.Player;
import main.PlayerHandler;
import main.PlayerHandler.CharacterCreateStatus;

/* This class package network packets */
public class NetProtocol {
	public enum LoginStatus{
		LOGIN_OK, LOGIN_FAIL
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
	
}
