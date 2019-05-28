package com.sms.server.stream.cdn;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.ContextBean;
import com.sms.server.ScopeContextBean;
import com.sms.server.api.IBasicScope;
import com.sms.server.api.IScope;
import com.sms.server.messaging.IMessageInput;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.InMemoryPullPullPipe;
import com.sms.server.net.http.codec.QueryStringDecoder;
import com.sms.server.stream.IBroadcastScope;
import com.sms.server.stream.ProviderService;
import com.sms.server.stream.proxy.BaseRTMPProxyStream;
import com.sms.server.stream.timeshift.TimeshiftingProvider;

/**
 * CDN提供者
 * @author pengliren
 *
 */
public class CDNProviderService extends ProviderService {

	private Logger log = LoggerFactory.getLogger(CDNProviderService.class);

	private static final class SingletonHolder {

		private static final CDNProviderService INSTANCE = new CDNProviderService();
	}

	protected CDNProviderService() {

	}

	public static CDNProviderService getInstance() {

		return SingletonHolder.INSTANCE;
	}

	@Override
	public IMessageInput getVODProviderInput(IScope scope, String name) {

		// http://ip:port/app/stream?starttime=20120901000000
		// rtmp://ip:port/app/stream?starttime=20120901000000
		// rtsp://ip:port/app/stream?starttime=20120901000000
		IPipe pipe = new InMemoryPullPullPipe();
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(name);
		String streamName = queryStringDecoder.getPath();
		Map<String, List<String>> params = queryStringDecoder.getParameters();
		if(!name.contains(":") && !params.isEmpty() && params.get("starttime") != null && params.get("starttime").size() > 0){
			ContextBean ctxBean = scope.getContext().getScopeCtxBean().getClazz(ScopeContextBean.BROADCASTSTREAM_BEAN); 
			String storePath = ctxBean.getProperty("path");			
			pipe.subscribe(new TimeshiftingProvider(storePath, streamName, params.get("starttime").get(0)), null);
			return pipe;
		} 
		
		return super.getVODProviderInput(scope, streamName);
	}

	@Override
	public INPUT_TYPE lookupProviderInput(IScope scope, String name, int type) {
		
		if(name.contains("starttime")) return INPUT_TYPE.VOD;	
		INPUT_TYPE result = super.lookupProviderInput(scope, name, type);
		
		if(result == INPUT_TYPE.LIVE){
			IBasicScope bs = scope.getBasicScope(IBroadcastScope.TYPE, name);
			Object s = bs.getAttribute(IBroadcastScope.STREAM_ATTRIBUTE);
			if(s == null){
			   result = INPUT_TYPE.NOT_FOUND;
			}else{
				if(s instanceof BaseRTMPProxyStream){
					log.debug("live proxy stream");
					if(((BaseRTMPProxyStream)s).isClosed()){
						log.info("found proxy stream but close");
						result = INPUT_TYPE.NOT_FOUND ;
						bs.removeAttribute(name);
					    scope.removeChildScope(bs);
					}
				}
			}
		}
		
		/*
		if(result == INPUT_TYPE.NOT_FOUND || result == INPUT_TYPE.LIVE_WAIT){			
			
			 /*			
			// TODO 这里需要根据接口获取相关参数 然后从其他服务器拉取到该流过来播放
			BaseRTMPProxyStream stream = new BaseRTMPProxyStream();
			stream.setScope(scope);			
			stream.start();	
			*/	
		/*	return INPUT_TYPE.LIVE;
		}*/
		log.debug("stream name :{},result:{}",new Object[]{name,result});
		return result;
	}
}
