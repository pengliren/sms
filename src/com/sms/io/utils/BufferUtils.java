package com.sms.io.utils;


import org.apache.mina.core.buffer.IoBuffer;

/**
 * Buffer Utility class which reads/writes intergers to the input/output buffer
 */
public class BufferUtils {

	//private static Logger log = LoggerFactory.getLogger(BufferUtils.class);

	/**
	 * Writes a Medium Int to the output buffer
	 * 
	 * @param out
	 *            Container to write to
	 * @param value
	 *            Integer to write
	 */
	public static void writeMediumInt(IoBuffer out, int value) {
		byte[] bytes = new byte[3];
		bytes[0] = (byte) ((value >>> 16) & 0x000000FF);
		bytes[1] = (byte) ((value >>> 8) & 0x000000FF);
		bytes[2] = (byte) (value & 0x00FF);
		out.put(bytes);
	}

	/**
	 * Reads an unsigned Medium Int from the in buffer
	 * 
	 * @param in
	 *            Source
	 * @return int Integer value
	 */
	public static int readUnsignedMediumInt(IoBuffer in) {
		byte[] bytes = new byte[3];
		in.get(bytes);
		int val = 0;
		val += (bytes[0] & 0xFF) * 256 * 256;
		val += (bytes[1] & 0xFF) * 256;
		val += (bytes[2] & 0xFF);
		return val;
	}

	/**
	 * Reads a Medium Int to the in buffer
	 * 
	 * @param in
	 *            Source
	 * @return int Medium int
	 */
	public static int readMediumInt(IoBuffer in) {
		byte[] bytes = new byte[3];
		in.get(bytes);
		int val = 0;
		val += bytes[0] * 256 * 256;
		val += bytes[1] * 256;
		val += bytes[2];
		if (val < 0) {
			val += 256;
		}
		return val;
	}

	/**
	 * Puts input buffer stream to output buffer and returns number of bytes
	 * written
	 * 
	 * @param out
	 *            Output buffer
	 * @param in
	 *            Input buffer
	 * @param numBytesMax
	 *            Number of bytes max
	 * @return int Number of bytes written
	 */
	@SuppressWarnings("unused")
	public final static int put(IoBuffer out, IoBuffer in, int numBytesMax) {
		// log.trace("Put - out buffer: {} in buffer: {} max bytes: {}", new
		// Object[]{out, in, numBytesMax});
		int limit = in.limit();
		int capacity = in.capacity();
		int numBytesRead = (numBytesMax > in.remaining()) ? in.remaining()
				: numBytesMax;
		// log.trace("limit: {} capacity: {} bytes read: {}", new
		// Object[]{limit, capacity, numBytesRead});
		// buffer.limit
		// The new limit value, must be non-negative and no larger than this
		// buffer's capacity
		// http://java.sun.com/j2se/1.4.2/docs/api/java/nio/Buffer.html#limit(int);
		// This is causing decoding error by raising RuntimeException
		// IllegalArgumentError in
		// RTMPProtocolDecoder.decode to ProtocolException.
		int thisLimit = (in.position() + numBytesRead <= in.capacity()) ? in
				.position() + numBytesRead : capacity;
		// somehow the "in" buffer becomes null here occasionally
		if (in != null) {
			in.limit(thisLimit);
			// any implication to giving output buffer in with limit set to
			// capacity?
			// Reduces numBytesRead, triggers continueDecode?
			out.put(in);
		} else {
			numBytesRead = 0;
		}
		in.limit(limit);
		return numBytesRead;
	}

	public static int byteArrayToInt(byte[] paramArrayOfByte) {
		return paramArrayOfByte[0] << 24 | (paramArrayOfByte[1] & 0xFF) << 16
				| (paramArrayOfByte[2] & 0xFF) << 8 | paramArrayOfByte[3]
				& 0xFF;
	}

	public static int byteArrayToInt(byte[] paramArrayOfByte, int paramInt) {
		return paramArrayOfByte[(paramInt + 0)] << 24
				| (paramArrayOfByte[(paramInt + 1)] & 0xFF) << 16
				| (paramArrayOfByte[(paramInt + 2)] & 0xFF) << 8
				| paramArrayOfByte[(paramInt + 3)] & 0xFF;
	}

	public static int byteArrayToInt(byte[] paramArrayOfByte, int paramInt1,
			int paramInt2) {
		return byteArrayToInt(paramArrayOfByte, paramInt1, paramInt2, false);
	}

	public static int byteArrayToInt(byte[] paramArrayOfByte, int paramInt1,
			int paramInt2, boolean paramBoolean) {
		int i = 0;
		for (int j = 0; j < paramInt2; j++) {
			if (j > 0)
				i <<= 8;
			if (paramBoolean)
				i |= paramArrayOfByte[(paramInt1 + (paramInt2 - (j + 1)))] & 0xFF;
			else
				i |= paramArrayOfByte[(paramInt1 + j)] & 0xFF;
		}
		return i;
	}
	
	public static byte[] longToByteArray(long paramLong) {
		return longToByteArray(paramLong, 8);
	}
	
	public static void longToByteArray(long paramLong, byte[] paramArrayOfByte,
			int paramInt1, int paramInt2) {
		for (int i = 0; i < Math.min(paramInt2, 8); i++) {
			int j = paramInt2 - i - 1;
			paramArrayOfByte[(paramInt1 + j)] = (byte) (int) (paramLong & 0xFF);
			paramLong >>= 8;
		}
	}
	
	public static byte[] longToByteArray(long paramLong, int paramInt) {
		byte[] arrayOfByte = new byte[paramInt];
		for (int i = 0; i < Math.min(paramInt, 8); i++) {
			int j = paramInt - i - 1;
			arrayOfByte[j] = (byte) (int) (paramLong & 0xFF);
			paramLong >>= 8;
			if (paramLong == 0L)
				break;
		}
		return arrayOfByte;
	}
	
	public static void intToByteArray(int paramInt1, byte[] paramArrayOfByte,
			int paramInt2, int paramInt3) {
		intToByteArray(paramInt1, paramArrayOfByte, paramInt2, paramInt3, false);
	}

	public static void intToByteArray(int paramInt1, byte[] paramArrayOfByte,
			int paramInt2, int paramInt3, boolean paramBoolean) {
		for (int i = 0; i < Math.min(paramInt3, 4); i++) {
			int j = paramBoolean ? i : paramInt3 - (i + 1);
			paramArrayOfByte[(paramInt2 + j)] = (byte) (paramInt1 & 0xFF);
			paramInt1 >>= 8;
		}
	}
}
