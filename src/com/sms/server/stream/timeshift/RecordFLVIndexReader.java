package com.sms.server.stream.timeshift;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Record FLV Idx file Reader
 * @author pengliren
 * 
 */
public class RecordFLVIndexReader {

	private static Logger log = LoggerFactory.getLogger(RecordFLVIndexReader.class);
	
	private RandomAccessFile idxFile;

	private FileChannel fc;
	
	private boolean opened = false;

	public RecordFLVIndexReader(String path) {

		try {
			idxFile = new RandomAccessFile(path, "r");
			opened = true;
		} catch (FileNotFoundException e) {			
			log.info("idx FileNotFoundException ");
			opened = false;
		}
		
		fc = idxFile.getChannel();
	}
	
	public KeyFramePosition getPosition(long timestamp) {
		
		if(!opened) return null;		
		int fintTS =(int) (timestamp*1000);
		KeyFramePosition pos = new KeyFramePosition();
		int count=0;
		try{
			fc.position(0);
			count = (int)(fc.size()/12);
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(count==0) return pos;
		log.debug("count:{}",count);
		try{
			ByteBuffer buff = ByteBuffer.allocate(12);
			
			for(int i=0;i<count;i++){
				fc.read(buff);
				buff.flip();
				int ts = buff.getInt();
				long position = buff.getLong();
				pos.setTimestamp(ts);
				pos.setPosition(position);
				if(ts>=fintTS){
					return pos;
				}
				buff.clear();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return pos;
	}
	
	public void close() {
		
		if(opened) {
			try {
				fc.close();
				idxFile.close();				
			} catch (IOException e) {
				log.info("close file fail");
			}
			opened = false;
		}
	}
}
