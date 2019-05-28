package com.sms.server.util;

import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;

import org.apache.commons.codec.digest.DigestUtils;

public class NetworkUtil {

	/**
	 * get local all mac array
	 * @return
	 */
	public static ByteBuffer getLocalMachinesArray() {

		ByteBuffer buff = ByteBuffer.allocate(512);
		Enumeration<?> allNetInterfaces = null;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
		}
		if (allNetInterfaces == null)
			return buff;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			try {
				if (!netInterface.isLoopback() && !netInterface.isVirtual()
						&& netInterface.getHardwareAddress() != null) {
					buff.put(netInterface.getHardwareAddress());
				}
			} catch (SocketException e) {
			}
		}
		buff.flip();
		return buff;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		ByteBuffer buff = getLocalMachinesArray();
		byte[] bytes = null;
		if(buff.hasRemaining()) {
			bytes = new byte[buff.limit()];
		}
		buff.get(bytes);
		System.out.println(DigestUtils.md5Hex(bytes));
	}
}
