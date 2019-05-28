package com.sms.server.stream.timeshift;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.IStreamableFile;
import com.sms.io.ITag;
import com.sms.io.ITagReader;
import com.sms.io.IoConstants;
import com.sms.io.flv.FLVHeader;
import com.sms.io.flv.impl.Tag;
import com.sms.io.utils.IOUtils;
import com.sms.server.util.FileUtil;

/**
 * FLV Reader From Memory
 * @author pengliren
 *
 */
public class FLVMemoryReader implements IoConstants, IRecordFLVReader {

	private static Logger log = LoggerFactory.getLogger(RecordFLVReaderCreator.class);
	
	private RecordFLVIndexReader flvIndexReader;
	
	private String flvFilePath;
	
	private IoBuffer in;
	
	private FLVHeader header;
	
	private long duration;
	
	public FLVMemoryReader(ByteBuffer buffer, String path) throws IOException {
			
		flvFilePath = path;
		in = IoBuffer.wrap(buffer);
		postInitialize();
	}
		
	//do not cache the keyframes
	private void postInitialize() {
		
		if (in.remaining() >= 9) {
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
		int pos = (int)flvIndexReader.getPosition(ts).getPosition()-4;
		in.position(pos);
		flvIndexReader.close();
		flvIndexReader = null;
	}
	
	@Override
	public void close() {
	
		if(flvIndexReader != null) {
			flvIndexReader.close();
		}
	}

	@Override
	public IStreamableFile getFile() {
		return null;
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public long getBytesRead() {
		return in.remaining();
	}

	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public long getTotalBytes() {
		return in.limit();
	}

	@Override
	public void decodeHeader() {
		// flv header is 9 bytes				
		header = new FLVHeader();
		// skip signature
		in.skip(3);
		header.setVersion(in.get());
		header.setTypeFlags(in.get());
		header.setDataOffset(in.getInt());
	}

	@Override
	public void position(long pos) {
		in.position((int)pos);
	}

	@Override
	public boolean hasMoreTags() {
		return in.remaining() > 4;
	}

	@Override
	public synchronized ITag readTag() {
		
		ITag tag = readTagHeader();
		if (tag != null) {
			byte[] data = new byte[tag.getBodySize()];
			in.get(data);
			tag.setBody(IoBuffer.wrap(data, 0, tag.getBodySize()));
		} else {
			log.debug("Tag was null");
		}
		return tag;
	}
	
	private ITag readTagHeader() {
		// previous tag size (4 bytes) + flv tag header size (11 bytes)	
		// previous tag's size
		int previousTagSize = in.getInt();
		// start of the flv tag
		byte dataType = in.get();
		// loop counter
		int i = 0;
		while (dataType != 8 && dataType != 9 && dataType != 18) {
			log.debug("Invalid data type detected, reading ahead");
			log.debug("Current position: {} limit: {}", in.position(), in.limit());
			// only allow 10 loops
			if (i++ > 10) {
				return null;
			}
			// move ahead and see if we get a valid datatype		
			dataType = in.get();
		}
		int bodySize = IOUtils.readUnsignedMediumInt(in);
		int timestamp = IOUtils.readExtendedMediumInt(in);
		if (log.isDebugEnabled()) {
			int streamId = IOUtils.readUnsignedMediumInt(in);
			log.debug("Data type: {} timestamp: {} stream id: {} body size: {} previous tag size: {}", new Object[] { dataType, timestamp, streamId, bodySize, previousTagSize });
		} else {
			in.skip(3);
		}
		return new Tag(dataType, timestamp, bodySize, null, previousTagSize);
	}

	@Override
	public boolean hasVideo() {

		return true;
	}

	@Override
	public ITagReader copy() {
		// TODO Auto-generated method stub
		return null;
	}
}
