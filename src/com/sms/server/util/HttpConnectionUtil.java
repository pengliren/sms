package com.sms.server.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConnectionUtil {

	private static Logger log = LoggerFactory.getLogger(HttpConnectionUtil.class);

	private static final String userAgent = "Mozilla/4.0 (compatible; SMS)";
	
	private static ThreadSafeClientConnManager connectionManager;
	
	private static int connectionTimeout = 7000;
	
	static {
		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		connectionManager = new ThreadSafeClientConnManager();
		connectionManager.setMaxTotal(40);
	}
	
	/**
	 * Returns a client with all our selected properties / params.
	 * 
	 * @return client
	 */
	public static final DefaultHttpClient getClient() {
		// create a singular HttpClient object
		DefaultHttpClient client = new DefaultHttpClient(connectionManager);
		// dont retry
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
		// get the params for the client
		HttpParams params = client.getParams();
		// establish a connection within x seconds
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, connectionTimeout);
		// no redirects
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		// set custom ua
		params.setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
		// set the proxy if the user has one set
		if ((System.getProperty("http.proxyHost") != null) && (System.getProperty("http.proxyPort") != null)) {
            HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost").toString(), Integer.valueOf(System.getProperty("http.proxyPort")));
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		return client;
	}
	
	/**
	 * post request
	 * @param url
	 * @param params
	 * @return
	 */
	public static String post(HttpClient httpClient, String url, NameValuePair... params) {

		List<NameValuePair> formparams = Arrays.asList(params);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, HTTP.UTF_8);
			HttpPost request = new HttpPost(url);
			request.setEntity(entity);
			request.addHeader(HttpHeaders.CONNECTION, "CLOSE");
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity resEntity = response.getEntity();
				return (resEntity == null) ? null : EntityUtils.toString(resEntity, HTTP.UTF_8);
			}
		} catch (UnsupportedEncodingException e) {
			log.info("http client post exception {}", e.getMessage());			
		} catch (ClientProtocolException e) {
			log.info("http client post exception {}", e.getMessage());
		} catch (IOException e) {
			log.info("http client post exception {}", e.getMessage());
		}
		return null;
	}
	
	public static String get(HttpClient httpClient, String url) {
	
		try {
			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.CONNECTION, "CLOSE");
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity resEntity = response.getEntity();
				return (resEntity == null) ? null : EntityUtils.toString(resEntity, HTTP.UTF_8);
			}
		} catch (UnsupportedEncodingException e) {
			log.info("http client post exception {}", e.getMessage());			
		} catch (ClientProtocolException e) {
			log.info("http client post exception {}", e.getMessage());
		} catch (IOException e) {
			log.info("http client post exception {}", e.getMessage());
		}
		return null;
	}
	
	/**
	 * Logs details about the request error.
	 * 
	 * @param response
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void handleError(HttpResponse response) throws ParseException, IOException {
		log.debug("{}", response.getStatusLine().toString());
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			log.debug("{}", EntityUtils.toString(entity));
		}
	}	
	
	/**
	 * @return the connectionTimeout
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * @param connectionTimeout the connectionTimeout to set
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		HttpConnectionUtil.connectionTimeout = connectionTimeout;
	}
}
