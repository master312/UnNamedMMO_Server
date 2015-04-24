package net;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ByteManager{
	public static byte[] shortToByte(short value){
		return ByteBuffer.allocate(2)
				.order(ByteOrder.BIG_ENDIAN)
				.putShort(value).array();
	}
	
	public static byte[] intToByte(int value){
		return ByteBuffer.allocate(4)
				.order(ByteOrder.BIG_ENDIAN)
				.putInt(value).array();
	}
	
	public static byte[] floatToByte(float value){
		return ByteBuffer.allocate(4).
				order(ByteOrder.BIG_ENDIAN).
				putFloat(value).array();
	}
	
	public static byte[] longToByte(long value){
		return ByteBuffer.allocate(8)
				.order(ByteOrder.BIG_ENDIAN)
				.putLong(value).array();
	}
	
	public static short byteToShort(byte bytes[]){
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
	}
	
	public static int byteToInt(byte bytes[]){
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
	}
	
	public static float byteToFloat(byte bytes[]){
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
	}
	
	public static long byteToLong(byte bytes[]){
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
	}
}