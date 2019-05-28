package com.sms.server.net.rtp;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * This class wraps a RTP packet providing method to convert from and to a
 * {@link IoBuffer}.
 * <p>
 * A RTP packet is composed of an header and the subsequent payload.
 * <p>
 * The RTP header has the following format:
 * 
 * <pre>
 *        0                   1                   2                   3
 *        0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        |V=2|P|X|  CC   |M|     PT      |       sequence number         |
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        |                           timestamp                           |
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *        |           synchronization source (SSRC) identifier            |
 *        +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
 *        |            contributing source (CSRC) identifiers             |
 *        |                             ....                              |
 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 * 
 * The first twelve octets are present in every RTP packet, while the list of
 * CSRC identifiers is present only when inserted by a mixer.
 * 
 * @author pengliren
 */
public class RTPPacket extends BaseRTPPacket {

	private int version = 2;
    private boolean padding = false;
    private boolean extensions = false;
    private int cc = 0;
    private volatile boolean marker = false;
    private int payloadType;
    private int seqNumber;
    private long timestamp;
    private long ssrc;
    private byte[] payload;
    private int offset = 0;
    private int length = 0;
    private long time;
    private long duration = -1;
    private boolean isValid = false;
    private byte[] buff;

	/**
	 * Construct a new RtpPacket reading the fields from a ByteBuffer
	 * 
	 * @param buffer
	 *            the buffer containing the packet
	 */

	public RTPPacket(IoBuffer buffer) {
		int len = buffer.limit();
        buff = new byte[len];
        buffer.get(buff, 0, len);

        int b = buff[0] & 0xff;

        version = (b & 0xC0) >> 6;
        padding = (b & 0x20) == 0x020;
        extensions = (b & 0x10) == 0x10;
        cc = b & 0x0F;

        b = buff[1] & 0xff;
        marker = (b & 0x80) == 0x80;
        payloadType = b & 0x7F;

        seqNumber = (buff[2] & 0xff) << 8;
        seqNumber = seqNumber | (buff[3] & 0xff);

        timestamp |= buff[4] & 0xFF;
        timestamp <<= 8;
        timestamp |= buff[5] & 0xFF;
        timestamp <<= 8;
        timestamp |= buff[6] & 0xFF;
        timestamp <<= 8;
        timestamp |= buff[7] & 0xFF;

        ssrc = (buff[8] & 0xff);
        ssrc <<= 8;
        ssrc |= (buff[9] & 0xff);
        ssrc <<= 8;
        ssrc |= (buff[10] & 0xff);
        ssrc <<= 8;
        ssrc |= (buff[11] & 0xff);

        payload = new byte[len - 12];
        System.arraycopy(buff, 12, payload, 0, payload.length);
	}

	public RTPPacket(byte payloadType, int seqNumber, int timestamp, long ssrc, byte[] payload) {
        this.payloadType = payloadType;
        this.payload = payload;
        this.seqNumber = seqNumber;
        this.timestamp = timestamp;
        this.ssrc = ssrc;
        this.offset = 0;
        this.length = payload.length;
    }
	
	public RTPPacket(boolean marker, byte payloadType, int seqNumber, int timestamp, long ssrc, byte[] payload, int offset, int length) {
        this.marker = marker;
        this.payloadType = payloadType;
        this.payload = payload;
        this.seqNumber = seqNumber;
        this.timestamp = timestamp;
        this.ssrc = ssrc;
        this.offset = offset;
        this.length = length;
        this.buff = new byte[payload.length + 12];
    }
	
	public RTPPacket() {	
	}

	/**
	 * Convert the packet instance into a {@link ByteBuffer} ready to be sent.
	 * 
	 * @return a new ByteBuffer
	 */
	public IoBuffer toByteBuffer() {
	
        return IoBuffer.wrap(toBytes());
	}
	
	public byte[] toBytes() {
		
		buff[0] = (byte) (version << 6);
        if (padding) {
            buff[0] = (byte) (buff[0] | 0x20);
        }

        if (extensions) {
            buff[0] = (byte) (buff[0] | 0x10);
        }

        buff[0] = (byte) (buff[0] | (cc & 0x0f));

        buff[1] = (byte) (payloadType);
        if (marker) {
            buff[1] = (byte) (buff[1] | 0x80);
        }

        buff[2] = ((byte) ((seqNumber & 0xFF00) >> 8));
        buff[3] = ((byte) (seqNumber & 0x00FF));

        buff[4] = ((byte) ((timestamp & 0xFF000000) >> 24));
        buff[5] = ((byte) ((timestamp & 0x00FF0000) >> 16));
        buff[6] = ((byte) ((timestamp & 0x0000FF00) >> 8));
        buff[7] = ((byte) ((timestamp & 0x000000FF)));

        buff[8] = ((byte) ((ssrc & 0xFF000000) >> 24));
        buff[9] = ((byte) ((ssrc & 0x00FF0000) >> 16));
        buff[10] = ((byte) ((ssrc & 0x0000FF00) >> 8));
        buff[11] = ((byte) ((ssrc & 0x000000FF)));
        System.arraycopy(payload, offset, buff, 12, length);
        return buff;
	}

	@Override
    public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("RTP Packet[marker=").append(marker);
		sb.append(", seq=").append(seqNumber);
		sb.append(", timestamp=").append(timestamp);
		sb.append(", payload_size=").append(payload.length);
		sb.append(", payload=").append(payloadType).append("]");
        return sb.toString();
    }

    public int getVersion() {
        return version;
    }

    public int getContributingSource() {
        return this.cc;
    }

    public boolean isPadding() {
        return padding;
    }

    public boolean isExtensions() {
        return extensions;
    }

    public long getSyncSource() {
        return this.ssrc;
    }
    
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public boolean getMarker() {
        return marker;
    }

    public int getPayloadType() {
        return payloadType;
    }

    public int getSeqNumber() {
        return this.seqNumber;
    }

    public byte[] getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }
    //defualt, visible for tests only. and RTP package.
    public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

	public void setPayload(byte[] payload) {
		this.length = payload.length;
		this.buff = new byte[payload.length + 12];
		this.payload = payload;
	}

	public long getSsrc() {
		return ssrc;
	}

	public void setSsrc(long ssrc) {
		this.ssrc = ssrc;
	}

	public int getLength() {
		return length;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setMarker(boolean marker) {
		this.marker = marker;
	}

	public void setPayloadType(int payloadType) {
		this.payloadType = payloadType;
	}

	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}

	public void setPadding(boolean padding) {
		this.padding = padding;
	}

	public void setExtensions(boolean extensions) {
		this.extensions = extensions;
	}
}
