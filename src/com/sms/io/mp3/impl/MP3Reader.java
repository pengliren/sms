package com.sms.io.mp3.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.sms.io.CachingFileKeyFrameMetaCache;
import com.sms.io.IKeyFrameMetaCache;
import com.sms.io.IStreamableFile;
import com.sms.io.ITag;
import com.sms.io.ITagReader;
import com.sms.io.IoConstants;
import com.sms.io.amf.Output;
import com.sms.io.flv.IKeyFrameDataAnalyzer;
import com.sms.io.flv.impl.Tag;

/**
 * Read MP3 files
 */
public class MP3Reader implements ITagReader, IKeyFrameDataAnalyzer {
	/**
	 * Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(MP3Reader.class);

	/**
	 * File
	 */
	private File file;

	/**
	 * File input stream
	 */
	private RandomAccessFile raf;
	
	private Bitstream bitStream;

	/**
	 * Last read tag object
	 */
	private ITag tag;

	/**
	 * Previous tag size
	 */
	private int prevSize;

	/**
	 * Current time
	 */
	private double currentTime;

	/**
	 * Frame metadata
	 */
	private KeyFrameMeta frameMeta;

	/**
	 * Positions and time map
	 */
	private HashMap<Long, Double> posTimeMap;

	private int dataRate;

	/**
	 * File duration
	 */
	private long duration;
	
	protected byte[] syncHeader = new byte[4];

	/**
	 * Frame cache
	 */
	static private IKeyFrameMetaCache frameCache = CachingFileKeyFrameMetaCache.getInstance();

	/**
	 * Holder for ID3 meta data
	 */
	private MetaData metaData;

	/**
	 * Container for metadata and any other tags that should
	 * be sent prior to media data.
	 */
	private LinkedList<ITag> firstTags = new LinkedList<ITag>();

	MP3Reader() {
		// Only used by the bean startup code to initialize the frame cache
	}

	/**
	 * Creates reader from file input stream
	 * @param file file input
	 * 
	 * @throws FileNotFoundException if not found 
	 */
	public MP3Reader(File file) throws FileNotFoundException {
		this.file = file;
		// parse the id3 info
		try {
			raf = new RandomAccessFile(file, "r");
			bitStream = new Bitstream(raf);
			Mp3File mp3file = new Mp3File(file);
			duration = mp3file.getLengthInSeconds();
			log.debug("Track length: {}", mp3file.getFrameCount());
			log.debug("Sample rate: {}", mp3file.getSampleRate());
			log.debug("Channels: {}", mp3file.getChannelMode());
			log.debug("Mpeg version: {}", mp3file.getVersion());
			log.debug("Mpeg layer: {}", mp3file.getLayer());
			log.debug("Original: {}", mp3file.isOriginal());
			log.debug("Copyrighted: {}", mp3file.isCopyright());
			log.debug("Bitrate: {}", mp3file.getBitrate());
			
			if (mp3file.hasId3v2Tag()) {
				ID3v2 id3v2Tag = mp3file.getId3v2Tag();
				metaData = new MetaData();
				metaData.setAlbum(id3v2Tag.getAlbum());
				metaData.setArtist(id3v2Tag.getArtist());
				metaData.setComment(id3v2Tag.getComment());
				metaData.setGenre(id3v2Tag.getGenreDescription());
				metaData.setSongName(id3v2Tag.getTitle());
				metaData.setTrack(id3v2Tag.getTrack());
				metaData.setYear(id3v2Tag.getYear());
				id3v2Tag.clearAlbumImage();
				/*
				byte[] imageBuffer = id3v2Tag.getAlbumImage();
				if (imageBuffer != null && imageBuffer.length > 0) {
					//set the cover image on the metadata
					metaData.setCovr(imageBuffer);
					// Create tag for onImageData event
					IoBuffer buf = IoBuffer.allocate(imageBuffer.length);
					buf.setAutoExpand(true);
					Output out = new Output(buf);
					out.writeString("onImageData");
					Map<Object, Object> props = new HashMap<Object, Object>();
					props.put("trackid", 1);
					props.put("data", imageBuffer);
					out.writeMap(props);
					buf.flip();
					//Ugh i hate flash sometimes!!
					//Error #2095: flash.net.NetStream was unable to invoke callback onImageData.
					ITag result = new Tag(IoConstants.TYPE_METADATA, 0, buf.limit(), null, 0);
					result.setBody(buf);
					//add to first frames
					firstTags.add(result);*/
			}
			
			if (mp3file.hasId3v1Tag()) {
				ID3v1 id3v1Tag = mp3file.getId3v1Tag();
				metaData = new MetaData();
				metaData.setAlbum(id3v1Tag.getAlbum());
				metaData.setArtist(id3v1Tag.getArtist());
				metaData.setComment(id3v1Tag.getComment());
				metaData.setGenre(id3v1Tag.getGenreDescription());
				metaData.setSongName(id3v1Tag.getTitle());
				metaData.setTrack(id3v1Tag.getTrack());
				metaData.setYear(id3v1Tag.getYear());
			} 
			
			if (!mp3file.hasId3v1Tag() && !mp3file.hasId3v2Tag()) {
				log.info("File did not contain ID3v1 or ID3v2 data: {}", file.getName());
			}
			
		} catch (Exception e) {
			log.error("MP3Reader {}", e);
			throw new RuntimeException(e);
		}
		
		analyzeKeyFrames();
		
		// Create file metadata object
		firstTags.addFirst(createFileMeta());
	}

	/**
	 * A MP3 stream never has video.
	 * 
	 * @return always returns <code>false</code>
	 */
	public boolean hasVideo() {
		return false;
	}

	public void setFrameCache(IKeyFrameMetaCache frameCache) {
		MP3Reader.frameCache = frameCache;
	}

	/**
	 * Creates file metadata object
	 * 
	 * @return Tag
	 */
	private ITag createFileMeta() {
		// Create tag for onMetaData event
		IoBuffer buf = IoBuffer.allocate(1024);
		buf.setAutoExpand(true);
		Output out = new Output(buf);
		out.writeString("onMetaData");
		Map<Object, Object> props = new HashMap<Object, Object>();
		props.put("duration", frameMeta.timestamps[frameMeta.timestamps.length - 1] / 1000.0);
		props.put("audiocodecid", IoConstants.FLAG_FORMAT_MP3);
		if (dataRate > 0) {
			props.put("audiodatarate", dataRate);
		}
		props.put("canSeekToEnd", true);
		//set id3 meta data if it exists
		if (metaData != null) {
			props.put("artist", metaData.getArtist());
			props.put("album", metaData.getAlbum());
			props.put("songName", metaData.getSongName());
			props.put("genre", metaData.getGenre());
			props.put("year", metaData.getYear());
			props.put("track", metaData.getTrack());
			props.put("comment", metaData.getComment());
			/*if (metaData.hasCoverImage()) {
				Map<Object, Object> covr = new HashMap<Object, Object>(1);
				covr.put("covr", new Object[] { metaData.getCovr() });
				props.put("tags", covr);
			}*/
			//clear meta for gc
			metaData = null;
		}
		out.writeMap(props);
		buf.flip();

		ITag result = new Tag(IoConstants.TYPE_METADATA, 0, buf.limit(), null, prevSize);
		result.setBody(buf);
		return result;
	}

	/** {@inheritDoc} */
	public IStreamableFile getFile() {
		return null;
	}

	/** {@inheritDoc} */
	public int getOffset() {
		return 0;
	}

	/** {@inheritDoc} */
	public long getBytesRead() {
		try {
			return raf.getFilePointer();
		} catch (IOException e) {
			log.error("getBytesRead : {}", e.getMessage());
		}
		return 0;
	}

	/** {@inheritDoc} */
	public long getDuration() {
		return duration;
	}

	/**
	 * Get the total readable bytes in a file or ByteBuffer.
	 * 
	 * @return Total readable bytes
	 */
	public long getTotalBytes() {
		try {
			return raf.length();
		} catch (IOException e) {
			log.error("getTotalBytes : {}", e.getMessage());
		}
		return 0;
	}

	/** {@inheritDoc} */
	public boolean hasMoreTags() {
		
		boolean flag = false;
		Header header;
		try {
			header = bitStream.readFrame();
			if(header != null && header.framesize > 0) {
				bitStream.unreadFrame();
				flag = true;
			}
		} catch (BitstreamException e) {
			bitStream.closeFrame();
		}
		return flag;
	}

	/** {@inheritDoc} */
	public synchronized ITag readTag() {
		if (!firstTags.isEmpty()) {
			// Return first tags before media data
			return firstTags.removeFirst();
		}

		Header header = null;
		try {
			header = bitStream.readFrame();
		} catch (BitstreamException e) {
			return null;
		}
		
		if (header == null) {
			return null;
		}

		int frameSize = header.framesize + 4;
		if (frameSize == 0) {
			// TODO find better solution how to deal with broken files...
			// See APPSERVER-62 for details
			return null;
		}

		tag = new Tag(IoConstants.TYPE_AUDIO, (int) currentTime, frameSize + 1, null, prevSize);
		prevSize = frameSize + 1;
		currentTime += header.ms_per_frame();
		IoBuffer body = IoBuffer.allocate(tag.getBodySize());
		body.setAutoExpand(true);
		byte tagType = (IoConstants.FLAG_FORMAT_MP3 << 4) | (IoConstants.FLAG_SIZE_16_BIT << 1);
		int channel = header.mode() == 3 ? 1 : 2;
		tagType += (channel - 1);
		header.getSyncHeader(this.syncHeader);
		if (header.frequency() >= 32000) {
			tagType |= 0x0C;
	      } else if (header.frequency() >= 22050) {
	    	  tagType |= 0x08;
	      } else if (header.frequency() >= 11025) {
	    	  tagType |= 0x04;
	      }
		
		/*switch (header.frequency()) {
			case 48000:
				tagType |= IoConstants.FLAG_RATE_48_KHZ << 2;
				break;
			case 44100:
				tagType |= IoConstants.FLAG_RATE_44_KHZ << 2;
				break;
			case 22050:
				tagType |= IoConstants.FLAG_RATE_22_KHZ << 2;
				break;
			case 11025:
				tagType |= IoConstants.FLAG_RATE_11_KHZ << 2;
				break;
			default:
				tagType |= IoConstants.FLAG_RATE_5_5_KHZ << 2;
		}*/
		//tagType |= (header.mode() != 3 ? IoConstants.FLAG_TYPE_STEREO : IoConstants.FLAG_TYPE_MONO);
		body.put(tagType);
		body.put(syncHeader, 0 , 4);
		body.put(bitStream.frame_bytes, 0, bitStream.framesize);
		body.flip();
		tag.setBody(body);
		bitStream.closeFrame();
		return tag;
	}

	/** {@inheritDoc} */
	public void close() {
		if (posTimeMap != null) {
			posTimeMap.clear();
		}
		
		if(bitStream != null) {
			try {
				bitStream.close();
			} catch (BitstreamException e) {
				log.error("close {}", e);
			}
		}
	}

	/** {@inheritDoc} */
	public void decodeHeader() {
	}

	/** {@inheritDoc} */
	public void position(long pos) {
		
		Double time = null;
		try {
			if (pos == Long.MAX_VALUE) {
				// Seek at EOF

				raf.seek(raf.length());

				currentTime = duration;
				return;
			}
			raf.seek((int) pos);
			// Advance to next frame
			bitStream.readFrame();
			bitStream.closeFrame();
			// Make sure we can resolve file positions to timestamps
			analyzeKeyFrames();
			time = posTimeMap.get(raf.getFilePointer());
		} catch (Exception e) {
			log.error("position {}", e);
		}
		if (time != null) {
			currentTime = time;
		} else {
			// Unknown frame position - this should never happen
			currentTime = 0;
		}
	}

	/** {@inheritDoc} 
	 * @throws IOException */
	public synchronized KeyFrameMeta analyzeKeyFrames() {
		
		if (frameMeta != null) {
			return frameMeta;
		}
		// check for cached frame informations
		if (frameCache != null) {
			frameMeta = frameCache.loadKeyFrameMeta(file);
			if (frameMeta != null && frameMeta.duration > 0) {
				// Frame data loaded, create other mappings
				duration = frameMeta.duration;
				frameMeta.audioOnly = true;
				posTimeMap = new HashMap<Long, Double>();
				for (int i = 0; i < frameMeta.positions.length; i++) {
					posTimeMap.put(frameMeta.positions[i], (double) frameMeta.timestamps[i]);
				}
				return frameMeta;
			}
		}
		List<Long> positionList = new ArrayList<Long>();
		List<Double> timestampList = new ArrayList<Double>();
		dataRate = 0;
		long rate = 0;
		int count = 0;
		double time = 0;
		try {
			bitStream.rewind();
		
			while (this.hasMoreTags()) {
				Header header = bitStream.readFrame();
				if (header == null || header.framesize == 0) {
					// TODO find better solution how to deal with broken files...
					// See APPSERVER-62 for details
					break;
				}			
				positionList.add(raf.getFilePointer());
				timestampList.add(time);
				rate += header.bitrate() / 1000;
				time += header.ms_per_frame();
				count++;
				bitStream.closeFrame();
			}
			bitStream.rewind();
		} catch (Exception e) {
			log.error("analyzeKeyFrames {}", e);
		}
		duration = (long) time;
		dataRate = (int) (rate / count);
		posTimeMap = new HashMap<Long, Double>();
		frameMeta = new KeyFrameMeta();
		frameMeta.duration = duration;
		frameMeta.positions = new long[positionList.size()];
		frameMeta.timestamps = new int[timestampList.size()];
		frameMeta.audioOnly = true;
		for (int i = 0; i < frameMeta.positions.length; i++) {
			frameMeta.positions[i] = positionList.get(i);
			frameMeta.timestamps[i] = timestampList.get(i).intValue();
			posTimeMap.put(positionList.get(i), timestampList.get(i));
		}
		if (frameCache != null) {
			frameCache.saveKeyFrameMeta(file, frameMeta);
		}
		return frameMeta;
	}

	/**
	 * Simple holder for id3 meta data
	 */
	static class MetaData {
		String album = "";

		String artist = "";

		String genre = "";

		String songName = "";

		String track = "";

		String year = "";

		String comment = "";

		byte[] covr = null;

		public String getAlbum() {
			return album;
		}

		public void setAlbum(String album) {
			this.album = album;
		}

		public String getArtist() {
			return artist;
		}

		public void setArtist(String artist) {
			this.artist = artist;
		}

		public String getGenre() {
			return genre;
		}

		public void setGenre(String genre) {
			this.genre = genre;
		}

		public String getSongName() {
			return songName;
		}

		public void setSongName(String songName) {
			this.songName = songName;
		}

		public String getTrack() {
			return track;
		}

		public void setTrack(String track) {
			this.track = track;
		}

		public String getYear() {
			return year;
		}

		public void setYear(String year) {
			this.year = year;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public byte[] getCovr() {
			return covr;
		}

		public void setCovr(byte[] covr) {
			this.covr = covr;
			log.debug("Cover image array size: {}", covr.length);
		}

		public boolean hasCoverImage() {
			return covr != null;
		}

	}

	@Override
	public ITagReader copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
