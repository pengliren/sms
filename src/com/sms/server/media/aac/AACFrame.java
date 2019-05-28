package com.sms.server.media.aac;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * AAC Frame
 * @author pengliren
 *
 */
public class AACFrame {

	private int rateIndex = 0;
	private int sampleRate = 0;
	private int size = 0;
	private int channelIndex = 0;
	private int channels = 0;
	private int rdb = 0;
	private int profileObjectType = 2;
	private boolean errorBitsAbsent = true;
	private IoBuffer data;
	private int dataLen;

	public int getSampleRate() {
		return this.sampleRate;
	}

	public void setSampleRate(int rate) {
		this.sampleRate = rate;
	}

	public int getSize() {
		return this.size;
	}

	public void setSize(int size) {
		this.size = size;
		this.dataLen = size - 7;
	}

	public int getChannels() {
		return this.channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public int getRdb() {
		return this.rdb;
	}

	public void setRdb(int rdb) {
		this.rdb = rdb;
	}

	public int getSampleCount() {
		return (this.rdb + 1) * 1024;
	}

	public int getRateIndex() {
		return this.rateIndex;
	}

	public void setRateIndex(int index) {
		this.rateIndex = index;
	}

	public int getChannelIndex() {
		return this.channelIndex;
	}

	public void setChannelIndex(int index) {
		this.channelIndex = index;
	}

	public boolean isErrorBitsAbsent() {
		return this.errorBitsAbsent;
	}

	public void setErrorBitsAbsent(boolean absent) {
		this.errorBitsAbsent = absent;
	}

	public int getProfileObjectType() {
		return this.profileObjectType;
	}

	public void setProfileObjectType(int profileType) {
		this.profileObjectType = profileType;
	}

	public IoBuffer getData() {
		return data;
	}

	public void setData(IoBuffer data) {
		this.data = data;
	}

	public void setDataLen(int dataLen) {
		this.dataLen = dataLen;
	}

	public int getDataLen() {
		
		if (size > 0) dataLen = size - 7;
		return dataLen;
	}

	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("{AACFrame: size: ").append(this.size);
		sb.append(", rate: ").append(this.sampleRate);
		sb.append(", channels: ").append(this.channels);
		sb.append(", samples: ").append(AACUtils.profileObjectTypeToString(this.profileObjectType));
		sb.append(", rateIdx: ").append(rateIndex).append("}");
		return sb.toString();
	}
}
