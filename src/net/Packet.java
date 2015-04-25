package net;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.esotericsoftware.kryo.serializers.FieldSerializer.Optional;
import com.esotericsoftware.minlog.Log;

public class Packet {
	/* Maximum packet size in bytes */
	@Optional(value = "")
	private static short readPos = 0;
	
	public byte data[];
	
	public Packet(){
		readPos = 0;
		data = null;
	}
	
	public void resetPointer(){
		readPos = 0;
	}
	
	public short readShort(){
		if(!safeCheck(readPos, (short)(readPos + 2))){
			Log.warn("Ilegal packet read call");
			return -1;
		}
		byte bytes[] = Arrays.copyOfRange(data, readPos, readPos + 2);
		readPos += 2;
		return ByteManager.byteToShort(bytes);
	}
	
	public int readInt(){
		if(!safeCheck(readPos, (short)(readPos + 4))){
			Log.warn("Ilegal packet read call");
			return -1;
		}
		byte bytes[] = Arrays.copyOfRange(data, readPos, readPos + 4);
		readPos += 4;
		return ByteManager.byteToInt(bytes);
	}
	
	public float readFloat(){
		if(!safeCheck(readPos, (short)(readPos + 4))){
			Log.warn("Ilegal packet read call");
			return -1f;
		}
		byte bytes[] = Arrays.copyOfRange(data, readPos, readPos + 4);
		readPos += 4;
		return ByteManager.byteToFloat(bytes);
	}
	
	public long readLong(){
		if(!safeCheck(readPos, (short)(readPos + 8))){
			Log.warn("Ilegal packet read call");
			return -1;
		}
		byte bytes[] = Arrays.copyOfRange(data, readPos, readPos + 8);
		readPos += 8;
		return ByteManager.byteToShort(bytes);
	}
	
	public String readString(){
		if(!safeCheck(readPos, (short)(readPos + 2))){
			Log.warn("Ilegal packet read call");
			return "";
		}
		byte bytes[] = Arrays.copyOfRange(data, readPos, readPos + 2);
		readPos += 2;
		
		
		int strLen = ByteManager.byteToShort(bytes);
		if(!safeCheck(readPos, (short)(readPos + strLen))){
			Log.warn("Ilegal packet read call");
			return "";
		}
		bytes = Arrays.copyOfRange(data, readPos, readPos + strLen);
		readPos += strLen;
		
		String str = new String(bytes, Charset.forName("UTF-8"));
		return str;
	}
	
	private boolean safeCheck(short start, short end){
		return start < data.length && end <= data.length;
	}
	
	/* This should be called when done using packet  */
	public void clear(){
		data = null;
		readPos = 0;
	}
}
