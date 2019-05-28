package com.sms.server.adapter;

import com.sms.server.api.IClient;
import com.sms.server.api.IConnection;
import com.sms.server.api.IScope;
import com.sms.server.api.stream.IBroadcastStream;

public class ApplicationAdapter extends MultiThreadedApplicationAdapter {

	/** {@inheritDoc} */
	@Override
	public synchronized boolean connect(IConnection conn, IScope scope, Object[] params) {
		return super.connect(conn, scope, params);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void disconnect(IConnection conn, IScope scope) {
		super.disconnect(conn, scope);
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized boolean start(IScope scope) {
		
		if("live".equals(scope.getName())) {			
			
			/*
			HTTPProxyStream client = new HTTPPullProxyStream("http://121.14.123.26:1863/2674956498.flv?apptype=live&pla=WIN&time=1351737698&cdn=zijian&vkey=3703382C092302DDE0FD6DB9E9C844F526E171BA2F1D3D93DFCD7F1E8675F563926107CF659504B1", "12345");
			client.setScope(scope);
			client.start();		
				
			UDPTSProxyStream tsProxy = new UDPTSProxyStream("225.0.0.1", 1234, "12345");
			tsProxy.setScope(scope);
			tsProxy.start();
			
			HTTPProxyStream client = new HTTPPullProxyStream("http://localhost:8080/test", "test");
			client.setScope(scope);
			client.start();
			
			RTMPProxyStream rtmpProxy = new RTMPProxyStream("219.232.160.246", 1935, "livestream", "szws", "c64024e7cd451ac19613345704f985fa");
			rtmpProxy.setScope(scope);
			rtmpProxy.start();		
				
			HTTPProxyStream client = new HTTPPullProxyStream("http://live-cdn1.smgbb.tv/channels/bbtv/dfws/flv:sd/live?1351576653703", "12345");
			client.setScope(scope);
			client.start();		
			
			HTTPProxyStream client = new HTTPPullProxyStream("http://v.ml.streamocean.com/vod/430E5393-A0DD-6DB2-DC49-8AEB18BD5E6E?fmt=x264_0K_flv&cpid=bf9b8e60478441aca76c1f6d776fd60b&sora=1&sk=8C6B37F1815EA45279761D71A83227D3&sora=1&timecode=0", "12345");
			client.setScope(scope);
			client.start();
			
			RTSPProxyStream client = new RTSPPullProxyStream("rtsp://192.168.10.123:1935/vod/flv:alizie.flv", "12345");
			client.setScope(scope);
			client.start();*/
		}
		
		return super.start(scope);
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized void stop(IScope scope) {
		super.stop(scope);
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized boolean join(IClient client, IScope scope) {
		return super.join(client, scope);
	}
	
	/** {@inheritDoc} */
	@Override
	public synchronized void leave(IClient client, IScope scope) {
		super.leave(client, scope);
	}
	
	@Override
	public void streamPublishStart(IBroadcastStream stream) {
		/*
		MpegtsSegmenterService ss = MpegtsSegmenterService.getInstance();
		stream.addStreamListener(ss);
		
		test multicast stream
		MulticastOutgoingService ms = MulticastOutgoingService.getInstance();
		UDPDatagramConfig config = new UDPDatagramConfig();
		config.setReceiveBufferSize(8192);
		config.setSendBufferSize(8192);
		ms.register(stream, config, "229.0.0.1", 1234);
		stream.addStreamListener(ms);
		super.streamPublishStart(stream);*/
	}
	
	@Override
	public void streamBroadcastClose(IBroadcastStream stream) {
		/*
		MpegtsSegmenterService ss = MpegtsSegmenterService.getInstance();
		stream.removeStreamListener(ss);
		ss.removeSegment(stream.getScope().getName(), stream.getPublishedName());
		
		MulticastOutgoingService ms = MulticastOutgoingService.getInstance();
		stream.removeStreamListener(ms);
		ms.unregister(stream.getPublishedName());
		super.streamBroadcastClose(stream);*/
	}
}
