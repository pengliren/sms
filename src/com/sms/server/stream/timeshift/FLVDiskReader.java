package com.sms.server.stream.timeshift;

import java.io.File;
import java.io.IOException;

import com.sms.io.flv.impl.FLVReader;
import com.sms.server.util.FileUtil;

/**
 * FLV Reader From Disk
 * @author pengliren
 *
 */
public class FLVDiskReader extends FLVReader implements IRecordFLVReader {

	private RecordFLVIndexReader flvIndexReader;
	
	private String flvFilePath;
	
	public FLVDiskReader(File f) throws IOException {
			
		super(f);
		flvFilePath = f.getAbsolutePath();
	}
	
	//do not cache the keyframes
	@Override
	protected void postInitialize() {
		
		if (getRemainingBytes() >= 9) {
			decodeHeader();
		}
	}
	
	@Override
	public void seekByTs(long ts) {
		
		if(flvIndexReader == null){
			String flvIndexPath = FileUtil.getFileName(flvFilePath) + ".idx"; 
			if(!(new File(flvIndexPath)).exists()){
				RecordFLVWriter.generateFlvIndexFile(flvFilePath);
			}
			flvIndexReader = new RecordFLVIndexReader(flvIndexPath);
			
		}
		setCurrentPosition(flvIndexReader.getPosition(ts).getPosition()-4);
		flvIndexReader.close();
		flvIndexReader = null;
	}
	
	@Override
	public void close() {
	
		super.close();
		
		if(flvIndexReader != null) {
			flvIndexReader.close();
		}
	}
}
