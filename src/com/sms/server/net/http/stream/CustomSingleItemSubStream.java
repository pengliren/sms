package com.sms.server.net.http.stream;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.IStreamableFile;
import com.sms.io.IStreamableFileFactory;
import com.sms.io.IStreamableFileService;
import com.sms.io.ITag;
import com.sms.io.ITagReader;
import com.sms.io.StreamableFileFactory;
import com.sms.io.flv.FLVUtils;
import com.sms.server.ScopeContextBean;
import com.sms.server.api.IBasicScope;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.api.scheduling.ISchedulingService;
import com.sms.server.api.stream.IAudioStreamCodec;
import com.sms.server.api.stream.IClientBroadcastStream;
import com.sms.server.api.stream.IClientStream;
import com.sms.server.api.stream.IPlayItem;
import com.sms.server.api.stream.IStreamCapableConnection;
import com.sms.server.api.stream.IStreamCodecInfo;
import com.sms.server.api.stream.IVideoStreamCodec;
import com.sms.server.api.stream.StreamState;
import com.sms.server.messaging.IMessageOutput;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.InMemoryPushPushPipe;
import com.sms.server.scheduling.QuartzSchedulingService;
import com.sms.server.stream.IBroadcastScope;
import com.sms.server.stream.IConsumerService;
import com.sms.server.stream.IProviderService;
import com.sms.server.stream.PlayEngine;
import com.sms.server.stream.SingleItemSubscriberStream;
import com.sms.server.stream.StreamNotFoundException;
import com.sms.server.stream.IProviderService.INPUT_TYPE;
import com.sms.server.stream.codec.AudioCodec;
import com.sms.server.stream.codec.VideoCodec;

public class CustomSingleItemSubStream extends SingleItemSubscriberStream {

	private static final Logger log = LoggerFactory.getLogger(CustomSingleItemSubStream.class);
	
	private ICustomPushableConsumer consumer;
	private IPlayItem item;
	private boolean isFailure;
	private boolean isLive;
	
	public CustomSingleItemSubStream(IScope scope, final ICustomPushableConsumer consumer) {
		
		this.setScope(scope);
		this.consumer = consumer;
		this.setClientBufferDuration(2000);
		this.setConnection((IStreamCapableConnection)consumer.getConnection());
		SMS.setConnectionLocal(consumer.getConnection());
	}
	
	@Override
	public IStreamCapableConnection getConnection() {
		return  (IStreamCapableConnection)consumer.getConnection();
	}
	
	@Override
	public void start() {
		
		// ensure the play engine exists
		if (engine == null) {
			IScope scope = getScope();
			if (scope != null) {
				ISchedulingService schedulingService = QuartzSchedulingService.getInstance();
				IConsumerService consumerService = new IConsumerService() {
					@Override
					public IMessageOutput getConsumerOutput(IClientStream stream) {
						IPipe pipe = new InMemoryPushPushPipe();
						pipe.subscribe(consumer, null);
						return pipe;
					}

				};
				IProviderService providerService = (IProviderService) scope.getContext().getService(ScopeContextBean.PROVIDERSERVICE_BEAN);
				engine = new PlayEngine.Builder(this, schedulingService, consumerService, providerService).build();
			} 
		}
		// set buffer check interval
		engine.setBufferCheckInterval(1000);
		// set underrun trigger
		engine.setUnderrunTrigger(5000);
		engine.setMaxPendingVideoFrames(2000);
		// Start playback engine
		engine.start();
		isFailure = false;
	}
	
	@Override
	public void play() throws IOException {
		try {
			engine.play(item);
		} catch (IllegalStateException e) {
			log.info(e.getMessage());
			isFailure = true;
		} catch (StreamNotFoundException e) {
			log.info(e.getMessage());
			isFailure = true;
		}
	}
	
	@Override
	public void close() {
		
		if(state != StreamState.CLOSED) {
			super.close();
		}
	}
	
	@Override
	public void onChange(StreamState state, Object... changed) {
		
		super.onChange(state, changed);		
		if(state == StreamState.STOPPED) {
			consumer.getConnection().close();					
		} else if(state == StreamState.PLAYING) {
			isLive = (Boolean) changed[1];
		}
	}

	@Override
	public void setPlayItem(IPlayItem item) {
		this.item = item;
		super.setPlayItem(item);
	}

	public boolean isFailure() {
		return isFailure;
	}

	public ICustomPushableConsumer getConsumer() {
		return consumer;
	}
	
	public boolean isLive() {
		
		return isLive;
	}
	
	public int getLastPlayTs() {
		
		Long ts = engine.getLastMessageTimestamp();
		return ts.intValue();
	}
	
	public INPUT_TYPE lookupStreamInput() {
		IScope scope = getScope();
		IProviderService providerService = (IProviderService) scope.getContext().getService(ScopeContextBean.PROVIDERSERVICE_BEAN);
		return providerService.lookupProviderInput(scope, item.getName(), 0);
	}	
	
	/**
	 * 
	 * @param videoConfig
	 * @param audioConfig
	 * @return
	 * @throws IOException 
	 */
	public void getConfig(IoBuffer videoConfig, IoBuffer audioConfig, AtomicLong duration) throws IOException {
		
		
		IScope scope = getScope();
		IProviderService providerService = (IProviderService) scope.getContext().getService(ScopeContextBean.PROVIDERSERVICE_BEAN);
		INPUT_TYPE result = lookupStreamInput();
		
		if(result == INPUT_TYPE.VOD) { // reader file get video and audio config
			File file = providerService.getVODProviderFile(scope, item.getName());
			if(file != null && file.exists()) {
				IStreamableFileFactory factory = StreamableFileFactory.getInstance();
				IStreamableFileService service = factory.getService(file);
				boolean audioChecked = false;
				boolean videoChecked = false;
				IStreamableFile streamFile = service.getStreamableFile(file);		
				ITagReader reader = streamFile.getReader();
				duration.set(reader.getDuration());
				ITag tag;
				int codec;
				for (int i = 0; i < 10; i++) {
					if (audioChecked && videoChecked) break;
					tag = reader.readTag();
					if (tag == null) return;
					if (ITag.TYPE_VIDEO == tag.getDataType()) {
						codec = FLVUtils.getVideoCodec(tag.getBody().get(0));
						if (codec == VideoCodec.AVC.getId() && tag.getBody().get(1) == 0x00) {
							videoChecked = true;
							videoConfig.put(tag.getBody());
							videoConfig.flip();
						}
					} else if (ITag.TYPE_AUDIO == tag.getDataType()) {
						codec = FLVUtils.getAudioCodec(tag.getBody().get(0));
						if ((codec == AudioCodec.AAC.getId() && tag.getBody().get(1) == 0x00) || codec == AudioCodec.MP3.getId()) {
							audioChecked = true;
							audioConfig.put(tag.getBody());
							audioConfig.flip();
						}
					}
				}
				reader.close();
			}
		} else if(result == INPUT_TYPE.LIVE) { // get live video and audio config
			IBasicScope basicScope = scope.getBasicScope(IBroadcastScope.TYPE, item.getName());
			IClientBroadcastStream bs = null;
			if(basicScope != null) {
				bs = (IClientBroadcastStream)basicScope.getAttribute(IBroadcastScope.STREAM_ATTRIBUTE);
			}
			
			if(bs != null) {
				IStreamCodecInfo codecInfo =  bs.getCodecInfo();
				IVideoStreamCodec videoCodecInfo = null;
				IAudioStreamCodec audioCodecInfo = null;
				if(codecInfo != null) {
					videoCodecInfo = codecInfo.getVideoCodec();
					audioCodecInfo = codecInfo.getAudioCodec();
				}
				
				if (videoCodecInfo != null && videoCodecInfo.getDecoderConfiguration() != null) {
					videoConfig.put(videoCodecInfo.getDecoderConfiguration());
					videoConfig.flip();
				}

				if (audioCodecInfo != null && audioCodecInfo.getDecoderConfiguration() != null) {
					audioConfig.put(audioCodecInfo.getDecoderConfiguration());
					audioConfig.flip();
				}
			}
		}
	}
	
	public IPlayItem getPlayItem() {
		return item;
	}
}
