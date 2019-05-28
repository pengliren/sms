package com.sms.server.net.rtmp.codec;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.net.rtmp.event.Aggregate;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.BytesRead;
import com.sms.server.net.rtmp.event.ChunkSize;
import com.sms.server.net.rtmp.event.FlexMessage;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.Ping;
import com.sms.server.net.rtmp.event.Unknown;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.so.ISharedObjectMessage;

/**
 * Event decoder decodes event objects from incoming byte buffer.
 */
public interface IEventDecoder {
    /**
     * Decodes event of Unknown type.
	 *
     * @param dataType               Data type
     * @param in                     Byte buffer to decode
     * @return                       Unknown event
     */
	public abstract Unknown decodeUnknown(byte dataType, IoBuffer in);

    /**
     * Decodes chunk size event.
	 *
     * @param in                     Byte buffer to decode
     * @return                       ChunkSize event
     */
	public abstract ChunkSize decodeChunkSize(IoBuffer in);

    /**
     * Decodes shared object message event.
	 *
     * @param in                     Byte buffer to decode
     * @param rtmp					 RTMP protocol state
     * @return                       ISharedObjectMessage event
     */
	public abstract ISharedObjectMessage decodeSharedObject(IoBuffer in, RTMP rtmp);

    /**
     * Decodes shared object message event from AMF3 encoding.
	 *
     * @param in                     Byte buffer to decode
     * @param rtmp					 RTMP protocol state
     * @return                       ISharedObjectMessage event
     */
	public abstract ISharedObjectMessage decodeFlexSharedObject(IoBuffer in, RTMP rtmp);

    /**
     * Decodes notification event.
	 *
     * @param in                     Byte buffer to decode
     * @param rtmp					 RTMP protocol state
     * @return                       Notify event
     */
    public abstract Notify decodeNotify(IoBuffer in, RTMP rtmp);

    /**
     * Decodes invocation event.
	 *
     * @param in                     Byte buffer to decode
     * @param rtmp					 RTMP protocol state
     * @return                       Invoke event
     */
    public abstract Invoke decodeInvoke(IoBuffer in, RTMP rtmp);

    /**
     * Decodes ping event.
	 *
     * @param in                     Byte buffer to decode
     * @return                       Ping event
     */
    public abstract Ping decodePing(IoBuffer in);

    /**
     * Decodes BytesRead event.
	 *
     * @param in                     Byte buffer to decode
     * @return                       BytesRead event
     */
    public abstract BytesRead decodeBytesRead(IoBuffer in);

	/**
	 * Decodes the aggregated data.
	 * 
	 * @param in                     Byte buffer to decode
     * @return                       Aggregate event
	 */
	public abstract Aggregate decodeAggregate(IoBuffer in);
    
    /**
     * Decodes audio data event.
	 *
     * @param in                     Byte buffer to decode
     * @return                       AudioData event
     */
    public abstract AudioData decodeAudioData(IoBuffer in);

    /**
     * Decodes video data event.
	 *
     * @param in                     Byte buffer to decode
     * @return                       VideoData event
     */
    public abstract VideoData decodeVideoData(IoBuffer in);

    /**
     * Decodes Flex message event.
	 *
     * @param in                     Byte buffer to decode
     * @param rtmp					 RTMP protocol state
     * @return                       FlexMessage event
     */
    public abstract FlexMessage decodeFlexMessage(IoBuffer in, RTMP rtmp);
}
