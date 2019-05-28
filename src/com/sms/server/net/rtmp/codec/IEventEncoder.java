package com.sms.server.net.rtmp.codec;

import org.apache.mina.core.buffer.IoBuffer;

import com.sms.server.net.rtmp.event.Aggregate;
import com.sms.server.net.rtmp.event.AudioData;
import com.sms.server.net.rtmp.event.BytesRead;
import com.sms.server.net.rtmp.event.ChunkSize;
import com.sms.server.net.rtmp.event.Invoke;
import com.sms.server.net.rtmp.event.Notify;
import com.sms.server.net.rtmp.event.Ping;
import com.sms.server.net.rtmp.event.Unknown;
import com.sms.server.net.rtmp.event.VideoData;
import com.sms.server.so.ISharedObjectMessage;

/**
 * Encodes events to byte buffer.
 */
public interface IEventEncoder {
    /**
     * Encodes Notify event to byte buffer.
	 *
     * @param notify         Notify event
     * @param rtmp			 RTMP protocol state
     * @return               Byte buffer
     */
	public abstract IoBuffer encodeNotify(Notify notify, RTMP rtmp);

    /**
     * Encodes Invoke event to byte buffer.
	 *
     * @param invoke         Invoke event
     * @param rtmp			 RTMP protocol state
     * @return               Byte buffer
     */
	public abstract IoBuffer encodeInvoke(Invoke invoke, RTMP rtmp);

    /**
     * Encodes Ping event to byte buffer.
	 *
     * @param ping           Ping event
     * @return               Byte buffer
     */
    public abstract IoBuffer encodePing(Ping ping);

    /**
     * Encodes BytesRead event to byte buffer.
	 *
     * @param streamBytesRead    BytesRead event
     * @return                   Byte buffer
     */
    public abstract IoBuffer encodeBytesRead(BytesRead streamBytesRead);

    /**
     * Encodes Aggregate event to byte buffer.
	 *
     * @param aggregate          Aggregate event
     * @return                   Byte buffer
     */
    public abstract IoBuffer encodeAggregate(Aggregate aggregate);    
    
    /**
     * Encodes AudioData event to byte buffer.
	 *
     * @param audioData          AudioData event
     * @return                   Byte buffer
     */
    public abstract IoBuffer encodeAudioData(AudioData audioData);

    /**
     * Encodes VideoData event to byte buffer.
	 *
     * @param videoData          VideoData event
     * @return                   Byte buffer
     */
    public abstract IoBuffer encodeVideoData(VideoData videoData);

    /**
     * Encodes Unknown event to byte buffer.
	 *
     * @param unknown            Unknown event
     * @return                   Byte buffer
     */
    public abstract IoBuffer encodeUnknown(Unknown unknown);

    /**
     * Encodes ChunkSize event to byte buffer.
	 *
     * @param chunkSize          ChunkSize event
     * @return                   Byte buffer
     */
    public abstract IoBuffer encodeChunkSize(ChunkSize chunkSize);

    /**
     * Encodes SharedObjectMessage event to byte buffer.
	 *
     * @param so                 ISharedObjectMessage event
     * @param rtmp				 RTMP protocol state
     * @return                   Byte buffer
     */
    public abstract IoBuffer encodeSharedObject(ISharedObjectMessage so, RTMP rtmp);

    /**
     * Encodes SharedObjectMessage event to byte buffer using AMF3 encoding.
	 *
     * @param so                 ISharedObjectMessage event
     * @param rtmp				 RTMP protocol state
     * @return                   Byte buffer
     */
    public IoBuffer encodeFlexSharedObject(ISharedObjectMessage so, RTMP rtmp);
}
