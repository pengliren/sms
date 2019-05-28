package com.sms.server.net.http.stream;

import java.security.Provider;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.utils.BufferUtils;

/**
 * MPEGTS Segment Encryptor
 * @author pengliren
 *
 */
public class MpegtsSegmentEncryptor {

	private static Logger log = LoggerFactory.getLogger(MpegtsSegmentEncryptor.class);
	
	public static boolean initProvider = true;
	
	private SecretKeySpec key;
	
	private Cipher cipher;
	
	public void init(byte[] encKeyBytes, int segmentSeq) {
		
		if (initProvider) {
			try {
				Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
				if (provider == null)
					Security.addProvider(new BouncyCastleProvider());
			} catch (Exception e) {
				log.info("init: addBouncyCastleSecurityProvider: {}", e.toString());
			}
			initProvider = false;
		}
		
		this.key = new SecretKeySpec(encKeyBytes, "AES");
		byte[] ivBytes = new byte[16];
		BufferUtils.longToByteArray(segmentSeq, ivBytes, 8, 8);
		IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		try {
			this.cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			this.cipher.init(Cipher.ENCRYPT_MODE, this.key, ivSpec);
		} catch (Exception e) {
			log.error("init: {}",  e.toString());
		}
	}

	public void close() {
		this.key = null;
		this.cipher = null;
	}

	public byte[] encryptChunk(byte[] data, int offset, int len) {
		byte[] encData = null;
		try {
			encData = this.cipher.update(data, offset, len);
		} catch (Exception e) {
			log.error("encryptChunk: {}", e.toString());
		}
		return encData;
	}
	
	public byte[] encryptFinal() {
		
		byte[] encData = null;
		try {
			encData = this.cipher.doFinal();
		} catch (Exception e) {
			log.error("encryptFinal: {}", e.toString());
		} 
		
		return encData;
	}
}
