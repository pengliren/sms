package com.sms.server.stream.timeshift;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.Configuration;
import com.sms.server.cache.CacheManager;
import com.sms.server.cache.ObjectCache;

/**
 * Record FLV Reader Creator
 * @author pengliren
 *
 */
public class RecordFLVReaderCreator {

	private static Logger log = LoggerFactory.getLogger(RecordFLVReaderCreator.class);
	
	private static ObjectCache fileCache;
	
	public synchronized static IRecordFLVReader createRecordFLVReader(File file) throws Exception {
	
		IRecordFLVReader reader = null;
		long memAvailable = Configuration.FILECACHE_MAXSIZE - getCacheSize();
		log.info("available file cache {} MB", memAvailable);	
		ByteBuffer data = (ByteBuffer)getFileCache().get(file.getAbsolutePath());
		if(data != null) { //cache reader
			ByteBuffer copy = data.asReadOnlyBuffer();
			if(file.length() > copy.remaining()) {
				copy = mapFile(file);
			}
			reader = new FLVMemoryReader(copy, file.getAbsolutePath());
		} else if(memAvailable > 0) { //system mem is enough put cache 
			data = mapFile(file);
			reader = new FLVMemoryReader(data, file.getAbsolutePath());
		} else { //system mem is not enough
			reader = new FLVDiskReader(file);
		}
		return reader;
	}
	
	private static ByteBuffer mapFile(File file) throws Exception {
		
		FileInputStream fileOutput = new FileInputStream(file); 
		FileChannel fileChannel = fileOutput.getChannel();
		ByteBuffer buff = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
		fileChannel.close();
		fileOutput.close();
		fileCache.put(file.getAbsolutePath(), buff, Configuration.FILECACHE_PURGE * 60);
		return buff.asReadOnlyBuffer();
	}
	
	public static ObjectCache getFileCache() {
		if (fileCache == null) {
			fileCache = CacheManager.getInstance().getCache("com.sms.server.stream.seek.fileCache");
		}

		return fileCache;
	}
	
	public static int getCacheSize() {
		
		Set<String> keys = getFileCache().getKeys();
		int total = 0;
		for(String key : keys) {
			if(getFileCache().get(key) != null) {
				ByteBuffer buff = (ByteBuffer)getFileCache().get(key);
				total += buff.limit();
			}
		}
		
		return Math.round(total / (1024 * 1024)) ;
	}
}
