package com.sms.io.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomGUID {

private static Logger log = LoggerFactory.getLogger(RandomGUID.class);
	
	private static final String hexChars = "0123456789ABCDEF";

	private static Random myRand;

	private static SecureRandom mySecureRand;

	private static String s_id;

	public String valueBeforeMD5 = "";

	public String valueAfterMD5 = "";

	/*
	 * Static block to take care of one time secureRandom seed.
	 * It takes a few seconds to initialize SecureRandom.  You might
	 * want to consider removing this static block or replacing
	 * it with a "time since first loaded" seed to reduce this time.
	 * This block will run only once per JVM instance.
	 */

	static {
		mySecureRand = new SecureRandom();
		long secureInitializer = mySecureRand.nextLong();
		myRand = new Random(secureInitializer);
		try {
			s_id = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			log.warn("Exception {}", e);
		}

	}

	/*
	 * Default constructor.  With no specification of security option,
	 * this constructor defaults to lower security, high performance.
	 */
	public RandomGUID() {
		getRandomGUID(false);
	}

	/*
	 * Constructor with security option.  Setting secure true
	 * enables each random number generated to be cryptographically
	 * strong.  Secure false defaults to the standard Random function seeded
	 * with a single cryptographically strong random number.
	 */
	public RandomGUID(boolean secure) {
		getRandomGUID(secure);
	}

	/*
	 * Method to generate the random GUID
	 */
	private void getRandomGUID(boolean secure) {
		MessageDigest md5 = null;
		StringBuilder sbValueBeforeMD5 = new StringBuilder();

		try {
			md5 = MessageDigest.getInstance("MD5");
			long time = System.currentTimeMillis();
			long rand = 0;

			if (secure) {
				rand = mySecureRand.nextLong();
			} else {
				rand = myRand.nextLong();
			}

			// This StringBuffer can be a long as you need; the MD5
			// hash will always return 128 bits.  You can change
			// the seed to include anything you want here.
			// You could even stream a file through the MD5 making
			// the odds of guessing it at least as great as that
			// of guessing the contents of the file!
			sbValueBeforeMD5.append(s_id);
			sbValueBeforeMD5.append(':');
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(':');
			sbValueBeforeMD5.append(Long.toString(rand));

			valueBeforeMD5 = sbValueBeforeMD5.toString();
			md5.update(valueBeforeMD5.getBytes());

			byte[] array = md5.digest();
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < array.length; ++j) {
				int b = array[j] & 0xFF;
				if (b < 0x10)
					sb.append('0');
				sb.append(Integer.toHexString(b));
			}

			valueAfterMD5 = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error: " + e);
		} catch (Exception e) {
			System.out.println("Error:" + e);
		}
	}

	/**
	 * Returns a byte array for the given uuid or guid.
	 * 
	 * @param uid
	 * @return array of bytes containing the id
	 */
	public final static byte[] toByteArray(String uid) {
		byte[] result = new byte[16];
		char[] chars = uid.toCharArray();
		int r = 0;
		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] == '-') {
				continue;
			}
			int h1 = Character.digit(chars[i], 16);
			++i;
			int h2 = Character.digit(chars[i], 16);
			result[(r++)] = (byte) ((h1 << 4 | h2) & 0xFF);
		}
		return result;
	}

	/**
	 * Returns a uuid / guid for a given byte array.
	 * 
	 * @param ba array of bytes containing the id
	 * @return id
	 */
	public static String fromByteArray(byte[] ba) {
		if ((ba != null) && (ba.length == 16)) {
			StringBuilder result = new StringBuilder(36);
			for (int i = 0; i < 16; ++i) {
				if ((i == 4) || (i == 6) || (i == 8) || (i == 10)) {
					result.append('-');
				}
				result.append(hexChars.charAt(((ba[i] & 0xF0) >>> 4)));
				result.append(hexChars.charAt((ba[i] & 0xF)));
			}
			return result.toString();
		}
		return null;
	}

	/**
	 * Returns a nice neat formatted string.
	 * 
	 * @param str unformatted string
	 * @return formatted string
	 */
	public static String getPrettyFormatted(String str) {
		return String.format("%s-%s-%s-%s-%s", new Object[] { str.substring(0, 8), str.substring(8, 12),
				str.substring(12, 16), str.substring(16, 20), str.substring(20) });
	}
	
	/*
	 * Convert to the standard format for GUID
	 * (Useful for SQL Server UniqueIdentifiers, etc.)
	 * Example: C2FEEEAC-CFCD-11D1-8B05-00600806D9B6
	 */
	public String toString() {
		return RandomGUID.getPrettyFormatted(valueAfterMD5.toUpperCase());
	}

	/*
	 * Demonstraton and self test of class
	 */
	public static void main(String args[]) {
		for (int i = 0; i < 100; i++) {
			RandomGUID myGUID = new RandomGUID();
			System.out.println("Seeding String=" + myGUID.valueBeforeMD5);
			System.out.println("rawGUID=" + myGUID.valueAfterMD5);
			System.out.println("RandomGUID=" + myGUID.toString());
		}
	}
}
