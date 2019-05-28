package com.sms.server.media.h264;

public class BitReader {
	private byte[] bytes = null;
	private int len = 0;
	private int currByte = 0;
	private int currIndex = -1;
	private int currBit = 8;
	private int count = 0;

	public BitReader(byte[] paramArrayOfByte) {
		this.bytes = paramArrayOfByte;
		this.len = this.bytes.length;
	}

	public BitReader(byte[] paramArrayOfByte, int paramInt) {
		this.bytes = paramArrayOfByte;
		this.len = paramInt;
	}

	public int remaining() {
		if (this.currIndex == -1)
			return this.len * 8;
		return 8 - this.currBit + (this.len - this.currIndex - 1) * 8;
	}

	public int count() {
		return this.count;
	}

	public int getBit() {
		int i = 0;
		if (this.currBit >= 8) {
			this.currIndex += 1;
			this.currByte = this.bytes[this.currIndex];
			this.currBit = 0;
		}
		i = (this.currByte & 0x80) == 128 ? 1 : 0;
		this.count += 1;
		this.currBit += 1;
		this.currByte <<= 1;
		return i;
	}

	public void skip() {
		getBit();
	}

	public void skip(int paramInt) {
		for (int i = 0; i < paramInt; i++)
			getBit();
	}

	public int peekInt(int paramInt) {
		int i = 0;
		int j = this.currIndex;
		int k = this.currByte;
		int m = this.currBit;
		int n = this.count;
		for (int i1 = 0; i1 < paramInt; i1++) {
			i <<= 1;
			i += getBit();
		}
		this.currIndex = j;
		this.currByte = k;
		this.currBit = m;
		this.count = n;
		return i;
	}

	public int getInt(int paramInt) {
		int i = 0;
		for (int j = 0; j < paramInt; j++) {
			i <<= 1;
			i += getBit();
		}
		return i;
	}

	public long getLong(int paramInt) {
		long l = 0L;
		for (int i = 0; i < paramInt; i++) {
			l <<= 1;
			int j = getBit();
			l += j;
		}
		return l;
	}

	public int getIntSigned(int paramInt) {
		int i = 0;
		for (int j = 0; j < paramInt; j++) {
			i <<= 1;
			i += getBit();
			if ((j != 0) || (i != 1))
				continue;
			i = -1;
		}
		return i;
	}

	public int readExpGolombSigned() {
		int i = readExpGolomb();
		return (i & 0x1) == 1 ? (i + 1) / 2 : -(i / 2);
	}

	public int readExpGolomb() {
		int i = 0;
		for (;(getBit() == 0) && (i < 32); i++);
		return (1 << i) - 1 + getInt(i);
	}
}
