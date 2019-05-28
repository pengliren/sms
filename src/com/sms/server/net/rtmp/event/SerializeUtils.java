package com.sms.server.net.rtmp.event;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * The utility class provides conversion methods to ease the use of
 * byte arrays, Mina IoBuffers, and NIO ByteBuffers.
 */
public class SerializeUtils {

	public static byte[] ByteBufferToByteArray(IoBuffer buf) {
		byte[] byteBuf = new byte[buf.limit()];
		int pos = buf.position();
		buf.rewind();
		buf.get(byteBuf);
		buf.position(pos);
		return byteBuf;
	}
	
	public static byte[] NioByteBufferToByteArray(ByteBuffer buf) {
		byte[] byteBuf = new byte[buf.limit()];
		int pos = buf.position();
		buf.position(0);
		buf.get(byteBuf);
		buf.position(pos);
		return byteBuf;
	}	
	
	public static void ByteArrayToByteBuffer(byte[] byteBuf, IoBuffer buf) {
		buf.put(byteBuf);
		buf.flip();
	}
	
	public static void ByteArrayToNioByteBuffer(byte[] byteBuf, ByteBuffer buf) {
		buf.put(byteBuf);
		buf.flip();
	}
	
}