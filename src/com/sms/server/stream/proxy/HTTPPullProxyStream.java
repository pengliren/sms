package com.sms.server.stream.proxy;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.util.HttpConnectionUtil;

/**
 * http拉流
 * @author pengliren
 *
 */
public class HTTPPullProxyStream extends HTTPProxyStream {

	private static Logger log = LoggerFactory.getLogger(HTTPPullProxyStream.class);
	
	private final DefaultHttpClient httpClient = HttpConnectionUtil.getClient();
	
	private final String url;
	
	private static final int RECEIVE_TARGET_SIZE = 4096;
	
	private PollDataThread pollThread;
	
	private static volatile int threadCount = 0;
	
	public HTTPPullProxyStream(String url, String streamName) {
		
		super(streamName);
		this.url = url;
		httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	}
	
	@Override
	public void start() {
		
		if(getScope() == null) {
			throw new RuntimeException("scope is null!");
		}
		if(start) return;
		threadCount++;
		synchronized (lock) {
			super.start();
			register();
			start = true;
			pollThread = new PollDataThread();
			pollThread.setName("HTTPProxyStreamThread-"+threadCount);
			pollThread.start();
			connManager.register(publishedName, this);
		}
		
		log.info("http pull proxy stream {} is start!", getPublishedName());
	}
	
	class PollDataThread extends Thread {
		
		@Override
		public void run() {
			HttpGet httpGet = new HttpGet(url);
			try {
				HttpResponse response = httpClient.execute(httpGet);
				int code = response.getStatusLine().getStatusCode();
				if (code == HttpStatus.SC_OK) {
					InputStream input = response.getEntity().getContent();
					byte[] b = new byte[RECEIVE_TARGET_SIZE]; 
					int len = 0;
					while((len=input.read(b))!= -1 && !HTTPPullProxyStream.super.closed) {
						IoBuffer temp = IoBuffer.wrap(b, 0, len).setAutoExpand(true);
						handleMessage(temp);
					}
					input.close();
				} 				
			} catch (Exception e) {
				log.info("http pull proxy Exception {}", e.getCause());
			} finally {
				HTTPPullProxyStream.this.stop();
			}
		}
	}
}
