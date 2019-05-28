package com.sms.server.stream.timeshift;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.ITag;
import com.sms.io.flv.impl.FLVWriter;
import com.sms.server.util.CustomizableThreadFactory;
import com.sms.server.util.FileUtil;

/**
 * FLV Writer
 * @author pengliren
 *
 */
public class RecordFLVWriter extends FLVWriter {

	private static Logger log = LoggerFactory.getLogger(RecordFLVWriter.class);

	private DataOutputStream indexOps = null;
	
	private List<QueuedData> queue = Collections.emptyList();
	
	private static int schedulerThreadSize = 4;
	
	private static ScheduledExecutorService scheduledExecutorService;
	
	private volatile Future<?> writerFuture;
	
	private ReentrantReadWriteLock reentrantLock;

	private volatile Lock writeLock;

	private volatile Lock readLock;
	
	private int queueThreshold = 33;
	
	private int percentage = 25;

	private int sliceLength = (queueThreshold / (100 / percentage));
	
	static {
		scheduledExecutorService = Executors.newScheduledThreadPool(schedulerThreadSize, new CustomizableThreadFactory("RecordFLVWriterExecutor-"));
	}

	public RecordFLVWriter(File file, boolean append) {
		super(file, append);

		try {
			indexOps = new DataOutputStream(new FileOutputStream(
					new StringBuilder(file.getPath().substring(0,
							file.getPath().lastIndexOf('.'))).append(".idx")
							.toString()));
		} catch (FileNotFoundException e) {
			log.info("FileNotFoundException");
		}
		
		queue = new ArrayList<QueuedData>(40);
		// add associated locks
		reentrantLock = new ReentrantReadWriteLock();
		writeLock = reentrantLock.writeLock();
		readLock = reentrantLock.readLock();
	}

	public void putTag(ITag tag) {

		writeLock.lock();
		try {
			//add to the queue
			queue.add(new QueuedData(tag));
		} finally {
			writeLock.unlock();
		}
		
		int queueSize = 0;
		readLock.lock();
		try {
			queueSize = queue.size();
		} finally {
			readLock.unlock();
		}
		
		// when we reach the threshold, sort the entire queue and spawn a worker
		// to write a slice of the data
		if (queueSize >= queueThreshold) {
			Object writeResult = null;
			// check for existing future
			if (writerFuture != null) {
				try {
						// timeout 1/2 second for every slice entry
						long timeout = sliceLength * 500L;
						//wait n seconds for a result from the last writer
						writeResult = writerFuture.get(timeout, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						log.info("Exception waiting for write result {}", e.getMessage());
						return;
					} catch (ExecutionException e) {
						log.info("Exception waiting for write result {}", e.getMessage());
						return;
					} catch (TimeoutException e) {
						log.info("Exception waiting for write result {}", e.getMessage());
						return;
					}
					
			}
			log.debug("Write future result (expect null): {}", writeResult);
			// get the slice
			final QueuedData[] slice = new QueuedData[sliceLength];
			log.debug("Slice length: {}", slice.length);
			writeLock.lock();
			try {
				// sort the queue
				Collections.sort(queue);
				log.debug("Queue length: {}", queue.size());
				for (int q = 0; q < sliceLength; q++) {
					slice[q] = queue.remove(0);
				}
				log.debug("Queue length (after removal): {}", queue.size());
			} finally {
				writeLock.unlock();
			}
			writerFuture = scheduledExecutorService.submit(new Runnable() {
				public void run() {
					log.debug("Spawning queue writer thread");
					try {
						doWrites(slice);
					} catch (IOException e) {
						log.info("record write {}", e.getMessage());					
					}
				}
			});
		}
	}
	
	private void doWrites(QueuedData[] slice) throws IOException {
		
		//empty the queue
		ITag tag;
		for (QueuedData queued : slice) {
			tag = queued.tag;
			if (tag.getDataType() == ITag.TYPE_VIDEO) {

				if (indexOps != null && tag.getBody().get(0) == 0x17) {
					indexOps.writeInt(tag.getTimestamp());
					indexOps.writeLong(this.getBytesWritten());
				}
			}			
			super.writeTag(tag);
		}
		//clear and null-out
		slice = null;
	}
	
	public final void doWrites() throws IOException {
		QueuedData[] slice = null;
		writeLock.lock();
		try {
			slice = queue.toArray(new QueuedData[0]);
			queue.removeAll(Arrays.asList(slice));
		} finally {
			writeLock.unlock();
		}
		doWrites(slice);
	}

	@Override
	public void close() {
		
		if (writerFuture != null) {
			try {
				writerFuture.get();
			} catch (Exception e) {
				log.info("Exception waiting for write result on close", e);
			}		
			
			writerFuture.cancel(true);
			try {
				doWrites();
			} catch (IOException e) {
				log.info("Exception waiting for write result on close", e);
			}
		}
		writerFuture = null;
		super.close();
		if (indexOps != null) {
			try {
				indexOps.close();
			} catch (IOException e) {
				log.info("idx File close Exception");
			}
		}
		
		if (queue != null) {
			writeLock.lock();
			try {
				//clear the queue
				queue.clear();
				queue = null;
			} finally {
				writeLock.unlock();
			}
		}
	}
	
	public static void generateFlvIndexFile(String flvPath) {
		
		File file;
		DataOutputStream dos = null;
		RandomAccessFile raf =null;
		String indexFileName = null;
		try{
			file = new File(flvPath);
			raf= new RandomAccessFile(flvPath,"r");
			StringBuilder sb = new StringBuilder();
			sb.append(file.getParent())
				.append(File.separatorChar)
				.append(FileUtil.getFileName(file.getName()))
				.append(".idx");
			indexFileName = sb.toString();
			File indexFile = new File(indexFileName);
			if(indexFile.exists()) indexFile.delete();
			indexFile.createNewFile();
			dos = new DataOutputStream(new FileOutputStream(indexFile));
		}catch(FileNotFoundException e){
			log.info("index FileNotFoundException");
		} catch (IOException e) {			
			log.info("IOException {}", e.getMessage());
		}
		long currentPosition = 13;
		if(raf!=null){
			try{
				byte[] buff = new byte[13];
				
				while(true){
					if(currentPosition+13>raf.length()){
						break;
					}
					raf.seek(currentPosition);
					long p = raf.getFilePointer();
					raf.read(buff);
					int size=(((byte)buff[1]&0xff)<<16)+(((byte)buff[2]&0xff)<<8)+(buff[3]&0xff);
					
					int ts = (((byte)buff[4]&0xff)<<16)+(((byte)buff[5]&0xff)<<8)+(buff[6]&0xff)+(((byte)buff[7]&0xff)<<24);
					
					
					if((byte)(buff[0]&0xff)==0x09 && (byte)(buff[11]&0xff)==0x17 && (byte)(buff[12]&0xff)==0x01){
						dos.writeInt(ts);
						dos.writeLong(p);
					}
					currentPosition+=(size+15);
				}
			}catch(Exception e){
				log.info("reader flv Exception");
				return;
			}
		}
		try{
			dos.close();
			raf.close();
		}catch(Exception e){
			log.info("flv close Exception");
		}
	}
	
	private final static class QueuedData implements Comparable<QueuedData> {

		final ITag tag;
		
		final int timestamp;
		
		QueuedData(ITag tag) {
			
			this.tag = tag;
			timestamp = tag.getTimestamp();
		}
		
		@Override
		public int compareTo(QueuedData other) {
			
			if (timestamp > other.timestamp) {
				return 1;
			} else if (timestamp < other.timestamp) {
				return -1;
			}
			return 0;
		}
		
	}
}
