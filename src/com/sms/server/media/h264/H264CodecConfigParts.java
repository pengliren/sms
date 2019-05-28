package com.sms.server.media.h264;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.sms.io.utils.HexDump;

/**
 * H264 Codec Config Parts
 * @author pengliren
 * 
 */
public class H264CodecConfigParts {

	private byte[] sps = null;
	public List<byte[]> ppss = null;
	private byte[] profileLevel = null;

	public String getProfileLevelIdStr() {
		String str = "";
		if (this.profileLevel != null)
			str = HexDump.toHexString(profileLevel, 0, 3).replace(" ", "");
		return str;
	}
	
	public void addPPS(byte[] pps) {
		if (this.ppss == null)
			this.ppss = new ArrayList<byte[]>();
		this.ppss.add(pps);
	}
	
	public String getSpropParameterSetsStr() {
		StringBuilder sb = new StringBuilder();
		if(sps != null) sb.append(new String(Base64.encodeBase64(sps)));// sps
		for(byte[] pps : ppss) {
			sb.append(",");
			sb.append(new String(Base64.encodeBase64(pps)));// ppss
		}
		return sb.toString();
	}

	public byte[] getSps() {
		return sps;
	}

	public void setSps(byte[] sps) {
		this.sps = sps;
	}

	public List<byte[]> getPpss() {
		return ppss;
	}

	public byte[] getProfileLevel() {
		return profileLevel;
	}

	public void setProfileLevel(byte[] profileLevel) {
		this.profileLevel = profileLevel;
	}

}
