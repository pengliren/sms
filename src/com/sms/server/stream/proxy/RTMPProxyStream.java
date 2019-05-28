package com.sms.server.stream.proxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.amf.Input;
import com.sms.io.object.Deserializer;
import com.sms.server.api.event.IEvent;
import com.sms.server.api.service.IPendingServiceCall;
import com.sms.server.net.rtmp.ClientExceptionHandler;
import com.sms.server.net.rtmp.RTMPClient;
import com.sms.server.net.rtmp.RTMPMinaConnection;
import com.sms.server.net.rtmp.event.IRTMPEvent;
import com.sms.server.net.rtmp.event.Notify;

/**
 * RTMP远程拉流到本地服务器
 * @author pengliren
 *
 */
public class RTMPProxyStream extends BaseRTMPProxyStream {

	private static Logger log = LoggerFactory.getLogger(RTMPProxyStream.class);

	private RTMPClient rtmpClient;
	
	private String remoteHost;	
	
	private int remotePort;
	
	private String remoteApp;
	
	private String pullStream;
	
	public RTMPProxyStream(String url, String publishStream) throws URISyntaxException {
		
		URI uri = new URI(url);
		remoteHost = uri.getHost();
		if (uri.getPort() == -1) {
			remotePort = 1935;
		}
		else {
			remotePort = uri.getPort();
		}
		
		String[] params =  uri.getPath().split("/");
		if(params.length >= 3) {
			this.remoteApp = params[1];
			this.pullStream = params[2];
		} else {
			throw new URISyntaxException("path", "path size error");
		}
			
		this.publishedName = publishStream;
		
		
		init();
	}
	
	public RTMPProxyStream(String host,int port,String app,String publishStream, String pullStream) {
		
		this.remoteHost = host;
		this.remotePort = port;
		this.remoteApp = app;
		this.publishedName = publishStream;
		this.pullStream = pullStream;
		
		init();
	}
	
	private void init() {

		rtmpClient = new RTMPClient();
		rtmpClient.setConnectionClosedHandler(new Thread(){
			public void run(){
				log.info("rtmp client connect close!");
				RTMPProxyStream.this.stop();
			}
		});
		rtmpClient.setServiceProvider(new ServiceProvider());
		rtmpClient.setStreamEventDispatcher(this);
		rtmpClient.setExceptionHandler(new ClientExceptionHandler(){
         
			@Override
			public void handleException(Throwable throwable) {
				log.info("rtmp client exception");
				RTMPProxyStream.this.stop();			
			}});			
	}

	@Override
	public void resultReceived(IPendingServiceCall call) {
		log.info("handle call result:{}",call);
		String method = call.getServiceMethodName();
		if(method.equals("connect")){
			rtmpClient.createStream(this);
		}else if(method.equals("createStream")){
			rtmpClient.play((Integer)call.getResult(), pullStream, -1000, -1000);
		}
	}
	
	@Override
	public void stop() {
	
		if(this.closed) return;
		
		synchronized (lock) {
			super.stop();
		}
		rtmpClient.disconnect();
		getConnection().close();
		connManager.unregister(publishedName);
		start = false;
	}
	
	@Override
	public void start() {
		
		if(getScope() == null) {
			throw new RuntimeException("scope is null!");
		}		
		if(start) return;
		
		synchronized (lock) {
			
			rtmpClient.connect(remoteHost, remotePort, remoteApp,this);
			super.start();
			register();
			start = true;
			connManager.register(publishedName, this);
		}
	}
	
	@Override
	public void dispatchEvent(IEvent event) {
	
		if( !(event instanceof IRTMPEvent)) return;
		RTMPMinaConnection conn = (RTMPMinaConnection)event.getSource();
		if(conn != null && conn.getScope() == null ) {
			conn.connect(getScope());
		}
		if(event instanceof Notify){
				
			IoBuffer inBuff = ((Notify)(event)).getData().asReadOnlyBuffer();
			inBuff.rewind();
			Input input = new Input(inBuff);
			String action = Deserializer.deserialize(input, String.class);
			if(action.equals("onTimecode")) return;
		}
		IRTMPEvent rtmpEvent = (IRTMPEvent)event;		
		rtmpEvent.setTimestamp(rtmpEvent.getHeader().getTimer());
		
		super.dispatchEvent(event);
	}
	
	public class ServiceProvider{
		@SuppressWarnings("unchecked")
		public void onStatus(Object args){
			if(args instanceof Map){
				log.debug("receive status:{}",args);
				Map<String,String> o = (Map<String,String>)args;
				if(o.get("code").equals("NetStream.Play.UnpublishNotify") || o.get("code").equals("NetStream.Play.StreamNotFound")){
					stop();
				}
			}
		}
	}
}
