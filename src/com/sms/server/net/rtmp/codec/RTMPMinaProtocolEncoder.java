package com.sms.server.net.rtmp.codec;

import java.util.LinkedList;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IConnection;
import com.sms.server.api.SMS;
import com.sms.server.net.rtmp.RTMPConnection;
import com.sms.server.net.rtmp.protocol.ProtocolState;

/**
 * Mina protocol encoder for RTMP.
 */
public class RTMPMinaProtocolEncoder extends ProtocolEncoderAdapter {

	protected static Logger log = LoggerFactory.getLogger(RTMPMinaProtocolEncoder.class);
	
	private RTMPProtocolEncoder encoder = new RTMPProtocolEncoder();
	
	private int targetChunkSize = 2048;
	
	/** {@inheritDoc} */
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws ProtocolCodecException {

    	final ProtocolState state = (ProtocolState) session.getAttribute(ProtocolState.SESSION_KEY);

		RTMPConnection conn = (RTMPConnection) session.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		final IConnection prevConn = SMS.getConnectionLocal();
		conn.getWriteLock().lock();
		try {

			// We need to synchronize on the output and flush the
			// generated data to prevent two packages to the same channel
			// to be sent in different order thus resulting in wrong
			// headers being generated.
			final IoBuffer buf = encoder.encode(state, message);
			if (buf != null) {
				int requestedWriteChunkSize = conn.getState().getWriteChunkSize();
				log.trace("Requested chunk size: {} target chunk size: {}", requestedWriteChunkSize, targetChunkSize);
				if (buf.remaining() <= targetChunkSize * 2) {
					log.trace("Writing output data");
					out.write(buf);
				} else {
					/*
					LinkedList<IoBuffer> chunks = Chunker.chunk(buf, requestedWriteChunkSize, targetChunkSize);
					log.trace("Writing output data in {} chunks", chunks.size());
					for (IoBuffer chunk : chunks) {
						out.write(chunk);
					}
					chunks.clear();
					chunks = null;
					*/
					int sentChunks = Chunker.chunkAndWrite(out, buf, requestedWriteChunkSize, targetChunkSize);
					log.trace("Wrote {} chunks", sentChunks);
				}
			} else {
				log.trace("Response buffer was null after encoding");
			}
		} catch (Exception ex) {
			log.error("", ex);
		} finally {
			conn.getWriteLock().unlock();
			SMS.setConnectionLocal(prevConn);
		}
	}

    public RTMPProtocolEncoder getEncoder() {
		return encoder;
	}
    
	/**
	 * Setter for baseTolerance
	 * */
	public void setBaseTolerance(long baseTolerance) {
		encoder.setBaseTolerance(baseTolerance);
	}
	
	/**
	 * Setter for dropLiveFuture
	 * */
	public void setDropLiveFuture (boolean dropLiveFuture) {
		encoder.setDropLiveFuture(dropLiveFuture);
	}    
	
	/**
	 * @return the targetChunkSize
	 */
	public int getTargetChunkSize() {
		return targetChunkSize;
	}

	/**
	 * @param targetChunkSize the targetChunkSize to set
	 */
	public void setTargetChunkSize(int targetChunkSize) {
		this.targetChunkSize = targetChunkSize;
	}
	
	/**
	 * Output data chunker.
	 */
	private static final class Chunker {

		@SuppressWarnings("unused")
		public static LinkedList<IoBuffer> chunk(IoBuffer message, int chunkSize, int desiredSize) {
			LinkedList<IoBuffer> chunks = new LinkedList<IoBuffer>();
			int targetSize = desiredSize > chunkSize ? desiredSize : chunkSize;
			int limit = message.limit();
			do {
				int length = 0;
				int pos = message.position();
				while (length < targetSize && pos < limit) {
					byte basicHeader = message.get(pos);
					length += getDataSize(basicHeader) + chunkSize;
					pos += length;
				}
				int remaining = message.remaining();
				log.trace("Length: {} remaining: {} pos+len: {} limit: {}", new Object[] { length, remaining, (message.position() + length), limit });
				if (length > remaining) {
					length = remaining;
				}
				// add a chunk
				chunks.add(message.getSlice(length));
			} while (message.hasRemaining());
			return chunks;
		}

		public static int chunkAndWrite(ProtocolEncoderOutput out, IoBuffer message, int chunkSize, int desiredSize) {
			int sentChunks = 0;
			int targetSize = desiredSize > chunkSize ? desiredSize : chunkSize;
			int limit = message.limit();
			do {
				int length = 0;
				int pos = message.position();
				while (length < targetSize && pos < limit) {
					byte basicHeader = message.get(pos);
					length += getDataSize(basicHeader) + chunkSize;
					pos += length;
				}
				int remaining = message.remaining();
				log.trace("Length: {} remaining: {} pos+len: {} limit: {}", new Object[] { length, remaining, (message.position() + length), limit });
				if (length > remaining) {
					length = remaining;
				}
				// send it
				out.write(message.getSlice(length));
				sentChunks++;
			} while (message.hasRemaining());
			return sentChunks;
		}
		
		private static int getDataSize(byte basicHeader) {
			final int streamId = basicHeader & 0x0000003F;
			final int headerType = (basicHeader >> 6) & 0x00000003;
			int size = 0;
			switch (headerType) {
				case 0:
					size = 12;
					break;
				case 1:
					size = 8;
					break;
				case 2:
					size = 4;
					break;
				default:
					size = 1;
					break;
			}
			if (streamId == 0) {
				size += 1;
			} else if (streamId == 1) {
				size += 2;
			}
			return size;
		}
	}
}
