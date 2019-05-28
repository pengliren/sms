package com.sms.server.net.http.file;

import static com.sms.server.net.http.message.HTTPHeaders.isKeepAlive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.file.DefaultFileRegion;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;

import com.sms.server.ContextBean;
import com.sms.server.ScopeContextBean;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.net.http.BaseHTTPService;
import com.sms.server.net.http.HTTPMinaConnection;
import com.sms.server.net.http.IHTTPService;
import com.sms.server.net.http.message.HTTPHeaders;
import com.sms.server.net.http.message.HTTPRequest;
import com.sms.server.net.http.message.HTTPResponse;
import com.sms.server.net.http.message.HTTPResponseStatus;
import com.sms.server.util.MatcherUtil;
/**
 * http static file Service
 * @author pengliren
 *
 */
public class HTTPStaticFileService extends BaseHTTPService implements IHTTPService {

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    
	@Override
	public void setHeader(HTTPResponse resp) {
		
	}

	@Override
	public void handleRequest(HTTPRequest req, HTTPResponse resp, IScope scope) throws Exception {
		
		String method = req.getMethod().toString();
		if(!REQUEST_GET_METHOD.equalsIgnoreCase(method)) {
			sendError(req, resp, HTTPResponseStatus.METHOD_NOT_ALLOWED);
            return;
		}
		final HTTPMinaConnection conn = (HTTPMinaConnection)SMS.getConnectionLocal();
		final String uri = req.getUri();
        final String path = sanitizeUri(uri, scope);
        if (path == null) {
            sendError(req, resp, HTTPResponseStatus.FORBIDDEN);
            return;
        }
        
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            sendError(req, resp, HTTPResponseStatus.NOT_FOUND);
            return;
        }
        
        if (file.isDirectory()) {
        	sendError(req, resp, HTTPResponseStatus.NOT_FOUND);
            return;
        }
        
        if (!file.isFile()) {
            sendError(req, resp, HTTPResponseStatus.FORBIDDEN);
            return;
        }
        
        // Cache Validation
        String ifModifiedSince = req.getHeader(HTTPHeaders.Names.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.equals("")) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(req, resp);
                return;
            }
        }
        
        final RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            sendError(req, resp, HTTPResponseStatus.NOT_FOUND);
            return;
        }
       
        long fileLength = raf.length();
        HTTPHeaders.setContentLength(resp, fileLength);
        setContentTypeHeader(resp, file);
        setDateAndCacheHeaders(resp, file);
        conn.write(resp);       
       
        final boolean isKeepAlive = isKeepAlive(req); 
        
        WriteFuture writeFuture = conn.write(new DefaultFileRegion(raf.getChannel()));
        writeFuture.addListener(new IoFutureListener<WriteFuture>() {
            public void operationComplete(WriteFuture future) {
            	try {
					raf.close();
				} catch (IOException e) {
					log.info("file close exception : {}", e.getMessage());
				}
            	if(!isKeepAlive) {
            		conn.getHttpSession().close(true);
                }
            }
        });
	}
	
	private static String sanitizeUri(String uri, IScope scope) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                return null;
            }
        }

        if (!uri.startsWith("/")) {
            return null;
        }
        
        ScopeContextBean scopeCtxBean = scope.getContext().getScopeCtxBean();
        ContextBean ctxBean = scopeCtxBean.getClazz(ScopeContextBean.SECURITYFORBIDDEN_BEAN);
        String[] forbiddenList = new String[0];
        if(ctxBean != null && !StringUtils.isEmpty(ctxBean.getClassName())) {
        	forbiddenList = ctxBean.getClassName().split(";");
        }
        
        for(String forbidden : forbiddenList) {
         	if(MatcherUtil.match(forbidden, uri)) return null;
         }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);
        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + ".") ||
            uri.contains("." + File.separator) ||
            uri.startsWith(".") || uri.endsWith(".") ||
            uri.matches(".*[<>&\"].*")) {
            return null;
        } 

        // Convert to absolute path.
        StringBuilder absPathSB = new StringBuilder();
        absPathSB.append(System.getProperty("sms.root"));
        absPathSB.append(File.separator);
        absPathSB.append("webapps");
        if(scope.getName().equals("root")) {
        	absPathSB.append(File.separator);
        	absPathSB.append("root");
        } 
        absPathSB.append(File.separator);
        absPathSB.append(uri);
        return  absPathSB.toString();
    }
	
	private void setDateHeader(HTTPResponse resp) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		resp.setHeader(HTTPHeaders.Names.DATE, dateFormatter.format(time.getTime()));
	}
	
	private void sendNotModified(HTTPRequest req, HTTPResponse resp) {

		setDateHeader(resp);
		commitResponse(req, resp, null, HTTPResponseStatus.NOT_MODIFIED);
	}
	
	private void setContentTypeHeader(HTTPResponse resp, File file) {
        String extn = MimetypesFileTypeMap.getExtension(file.getPath());
        String type = MimetypesFileTypeMap.getContentType(extn); 
		if (type != null) {
			resp.setHeader(HTTPHeaders.Names.CONTENT_TYPE, MimetypesFileTypeMap.getContentType(extn));
		}
    }
	
	private void setDateAndCacheHeaders(HTTPResponse resp, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        resp.setHeader(HTTPHeaders.Names.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        resp.setHeader(HTTPHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
        resp.setHeader(HTTPHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        resp.setHeader(HTTPHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }
}
