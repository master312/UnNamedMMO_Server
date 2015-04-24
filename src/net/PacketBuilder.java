package net;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class PacketBuilder {	
	private ArrayList<byte[]> data = new ArrayList<byte[]>();
	private int size = 0;
	
	public PacketBuilder(){
	}
	
	public void writeShort(short value){
		data.add(ByteManager.shortToByte(value));
		size += 2;
	}
	
	public void writeInt(int value){
		data.add(ByteManager.intToByte(value));
		size += 4;
	}
	
	public void writeFloat(float value){
		data.add(ByteManager.floatToByte(value));
		size += 4;
	}
	
	public void writeLong(long value){
		data.add(ByteManager.longToByte(value));
		size += 8;
	}
	
	public void writeString(String value){
		byte tmp[] = value.getBytes(Charset.forName("UTF-8"));
		data.add(ByteManager.shortToByte((short)tmp.length));
		data.add(tmp);
		size += tmp.length + 2;
	}
	
	public Packet getPacket(){
		Packet tmp = new Packet();
		tmp.data = new byte[size];
		
		int tmpLen = 0;
		for(int i = 0; i < data.size(); i++){
			byte tmpB[] = data.get(i);
			System.arraycopy(tmpB, 0, tmp.data, tmpLen, tmpB.length);
			tmpLen += tmpB.length;
		}
		
		return tmp;
	}
}