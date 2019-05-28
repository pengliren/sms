package com.sms.server.net.http.stream;

import static com.sms.server.net.http.message.HTTPHeaders.Names.CONTENT_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;

import com.sms.io.IStreamableFile;
import com.sms.io.IStreamableFileFactory;
import com.sms.io.IStreamableFileService;
import com.sms.io.ITagReader;
import com.sms.io.StreamableFileFactory;
import com.sms.io.flv.IKeyFrameDataAnalyzer;
import com.sms.io.flv.IKeyFrameDataAnalyzer.KeyFrameMeta;
import com.sms.server.Configuration;
import com.sms.server.ScopeContextBean;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.net.http.BaseHTTPService;
import com.sms.server.net.http.HTTPMinaConnection;
import com.sms.server.net.http.IHTTPService;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;
import com.sms.server.stream.IProviderService;
import com.sms.server.stream.IProviderService.INPUT_TYPE;

/**
 * HTTP Live Stream M3U8 File Parse
 * @author pengliren
 *
 */
public class HTTPM3U8Service extends BaseHTTPService implements IHTTPService {

	@Override
	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception {

		String method = req.getMethod().toString();
		if (!REQUEST_GET_METHOD.equalsIgnoreCase(method) && !REQUEST_POST_METHOD.equalsIgnoreCase(method)) {
			// Bad request - return simple error page
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);
			return;
		}
		String path = req.getPath().substring(1);
		String[] segments = path.split("/");
		String app = scope.getName();
		String streamName;
		if (segments.length < 2) { // app/stream/playlist.m3u8
			sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);	
			return;
		}
		
		streamName = segments[0];	
		
		IProviderService providerService = (IProviderService) scope.getContext().getService(ScopeContextBean.PROVIDERSERVICE_BEAN);
		INPUT_TYPE result = providerService.lookupProviderInput(scope, streamName, 0);
		if(result == INPUT_TYPE.VOD) { 
			playVodStream(scope, app, streamName, req, resp);
		} else if(result == INPUT_TYPE.LIVE) {
			playLiveStream(scope, app, streamName, req, resp);
		} 
	}
	
	/**
	 *  play live stream by hls
	 * @param scope
	 * @param app
	 * @param streamName
	 * @param session
	 * @param resp
	 */
	private void playLiveStream(IScope scope, String app, String streamName, HTTPRequest req, HTTPResponse resp) {
		
		HTTPMinaConnection conn = (HTTPMinaConnection)SMS.getConnectionLocal();
		MpegtsSegmenterService service = MpegtsSegmenterService.getInstance();
		StringBuilder buff = new StringBuilder();
		buff.append("#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-ALLOW-CACHE:NO\n");
		if (service.isAvailable(scope, streamName)) {

			int count = service.getSegmentCount(app, streamName);
    		if (count <= 0) {
				long maxWaitTime = 2 * service.getSegmentTimeLimit();
				long start = System.currentTimeMillis();
				do {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						log.info("waiting thread interruped?");
						break;
					}
					if ((System.currentTimeMillis() - start) >= maxWaitTime) {
						log.info("Maximum segment wait time exceeded for {}", streamName);
						break;
					}
				} while ((count = service.getSegmentCount(app, streamName)) < 1);
    		}
    		
    		// get the count one last time
    		count = service.getSegmentCount(app, streamName);    		    	
    		if (count >= 1) {
        		//get segment duration in seconds
        		long segmentDuration = service.getSegmentTimeLimit() / 1000;  	    		
        		//get the current segment
        		List<MpegtsSegment> tsSegments = service.getSegmentList(app,streamName);
        		//get current sequence number
        		int sequenceNumber = tsSegments.get(0).getSequence();	
        		// create the heading            
                buff.append(String.format("#EXT-X-TARGETDURATION:%s\n#EXT-X-MEDIA-SEQUENCE:%s\n", segmentDuration, sequenceNumber));
                if(service.getSegmentIsEncrypt(app, streamName)) {
                	if(conn.getHttpSession().getLocalAddress() == null) return;
                    String address = conn.getHttpSession().getLocalAddress().toString();
                    String httpAes = String.format("http:/%s/%s/%s/aes", address, app, streamName);
                	buff.append("#EXT-X-KEY:METHOD=AES-128,URI=\"").append(httpAes).append("\"\n");
                }
                for(MpegtsSegment seg : tsSegments) {
                	buff.append(String.format("#EXTINF:%s, \n%s.ts?type=live\n", segmentDuration, seg.getSequence()));
                }                
    		} else {
    			log.info("Minimum segment count not yet reached, currently at: {}", count);
    		}
		} else {
			log.info("Stream: {} is not available", streamName);			
			buff.append("#EXT-X-ENDLIST\n");
		}
		IoBuffer data = IoBuffer.wrap(buff.toString().getBytes());
		setHeader(resp);
		commitResponse(req, resp, data);		
	}
	
	/**
	 * play vod stream by hls and file support flv format
	 * @param scope
	 * @param app
	 * @param streamName
	 * @param session
	 * @param resp
	 * @throws IOException 
	 */
	private void playVodStream(IScope scope, String app, String streamName, HTTPRequest req, HTTPResponse resp) {
		
		IProviderService providerService = (IProviderService) scope.getContext().getService(ScopeContextBean.PROVIDERSERVICE_BEAN);
		File file = providerService.getVODProviderFile(scope, streamName);
		
		if(file != null && file.exists()) {
			IStreamableFileFactory factory = StreamableFileFactory.getInstance();
			IStreamableFileService service = factory.getService(file);
			if (service != null && (StringUtils.endsWithIgnoreCase(streamName, ".flv") 
					|| StringUtils.endsWithIgnoreCase(streamName, ".mp4"))) {
				
				ITagReader reader = null;
				IStreamableFile streamFile;
				try {
					streamFile = service.getStreamableFile(file);
					reader = streamFile.getReader();
					HTTPTSService.getFileCache().put(streamName, reader, Configuration.FILECACHE_PURGE * 60);
				} catch (IOException e) {
					log.info("play hls exception {}", e.getMessage());
					sendError(req, resp, HTTPResponseStatus.BAD_REQUEST);		
					return;
				}
				
				KeyFrameMeta keymeta = ((IKeyFrameDataAnalyzer) reader).analyzeKeyFrames();
				long[] positions = keymeta.positions;
				int[] timestamps = keymeta.timestamps;
				int duration = Configuration.HLS_SEGMENT_TIME * 1000;
				int nextTime = duration;
				long startPos = positions[0];
				int rest = 0;
				StringBuilder sb = new StringBuilder("#EXTM3U\n#EXT-X-VERSION:3\n");
				sb.append("#EXT-X-TARGETDURATION:").append(Configuration.HLS_SEGMENT_TIME).append("\n");
				sb.append("#EXT-X-MEDIA-SEQUENCE:1\n");
				int seqNum = 1;
				float fixDuration = 0;
				for (int i = 0; i < positions.length; i++) {
					if (timestamps[i] >= nextTime) {
						fixDuration = timestamps[i] - nextTime;
						fixDuration = (duration + fixDuration) / 1000;
						rest = 0;
						sb.append("#EXTINF:").append(fixDuration).append(",\n");
						if (i == (positions.length - 1)) {
							sb.append(String.format("%s_%s_%d.ts?type=vod\n", startPos, file.length(), seqNum));
							seqNum++;
						} else {
							sb.append(String.format("%s_%s_%d.ts?type=vod\n", startPos, positions[i], seqNum));
							seqNum++;
						}
						startPos = positions[i];
						nextTime = timestamps[i] + duration; // fix next time
					} else rest++;
				}
				reader.close();
				// last time < duration
				if (rest > 0) {
					// last time = duration - (nexttime - timestamops(lastone))
					float lastOneDuration = (duration - (nextTime - timestamps[timestamps.length - 1])) / 1000;
					sb.append("#EXTINF:").append(lastOneDuration).append(",\n");
					sb.append(String.format("%s_%s_%d.ts?type=vod\n", startPos, file.length(), seqNum));
				}
				sb.append("#EXT-X-ENDLIST\n");
				IoBuffer data = IoBuffer.wrap(sb.toString().getBytes());
				setHeader(resp);
				commitResponse(req, resp, data);	
			} else {
				sendError(req, resp, HTTPResponseStatus.NOT_FOUND);
			}
		}
	}

	@Override
	public void setHeader(HTTPResponse resp) {
		
		resp.addHeader("Accept-Ranges", "bytes");
		resp.addHeader(CONTENT_TYPE, "application/vnd.apple.mpegurl");
		resp.addHeader("Pragma", "no-cache"); 
		resp.setHeader("Cache-Control", "no-cache");
	}

}
