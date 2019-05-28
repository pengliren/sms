package com.sms.server;

import java.util.List;

import com.sms.server.api.IScopeHandler;
import com.sms.server.net.http.IHTTPApplicationAdapter;
import com.sms.server.util.FileUtil;

public class ServerAppLoader {
	
	public void start() {
		
		Server server = Server.getInstance();
		String webappFolder = String.format("%s/%s", System.getProperty("sms.root"), "webapps");
		List<String> webApps = FileUtil.getDirectoryList(webappFolder);
		
		String appContextPath;
		String appName;
	
		for(int i = 0; i < webApps.size(); i++) {
			
			appName = webApps.get(i);
			appContextPath = String.format("%s/%s", webappFolder, appName);			
			WebScope webScope = new WebScope();
			Context webCtx = new Context();
			webCtx.setContextPath(appContextPath);
			webScope.setServer(server);
			webScope.setContext(webCtx);
			webScope.setParent(server.getGlobal("default"));
			webScope.setContextPath("/"+appName);
			webScope.setVirtualHosts("*");
			webCtx.setBean(Configuration.appConfigMap.get(appName));
			webScope.setHandler((IScopeHandler)webCtx.getBean(ScopeContextBean.RTMPAPPLICATIONADAPTER_BEAN));
			webScope.setHttpApplicationAdapter((IHTTPApplicationAdapter)webCtx.getBean(ScopeContextBean.HTTPAPPLICATIONADAPTER_BEAN));
			webScope.register();
		}
	}
	
}
