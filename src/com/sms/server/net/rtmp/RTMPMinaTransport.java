package com.sms.server.net.rtmp;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.Configuration;
import com.sms.server.util.CustomizableThreadFactory;

/**
 * 
* @ClassName: RTMPMinaTransport
* @Description: rtmp服务启动
* @author pengliren
*
 */
public class RTMPMinaTransport {

	private static final Logger log = LoggerFactory.getLogger(RTMPMinaTransport.class);
	
	private SocketAcceptor acceptor;
	private IoHandler ioHandler;
	protected boolean useHeapBuffers = true;
	
	public void start() throws IOException {
		log.info("RTMP Mina Transport starting...");
		if (useHeapBuffers) {
			// dont pool for heap buffers
			IoBuffer.setAllocator(new SimpleBufferAllocator());
		}
		
		acceptor = new NioSocketAcceptor(Configuration.RTMP_IO_THREADS);		
		ioHandler = new RTMPMinaIoHandler();
		acceptor.setHandler(ioHandler);
		acceptor.setBacklog(Configuration.RTMP_MAX_BACKLOG);
		
		SocketSessionConfig sessionConf = acceptor.getSessionConfig();
		//reuse the addresses
		sessionConf.setReuseAddress(true); 
		sessionConf.setTcpNoDelay(Configuration.RTMP_TCP_NODELAY);
		sessionConf.setReceiveBufferSize(Configuration.RTMP_RECEIVE_BUFFER_SIZE);
		sessionConf.setMaxReadBufferSize(Configuration.RTMP_RECEIVE_BUFFER_SIZE);
		sessionConf.setSendBufferSize(Configuration.RTMP_SEND_BUFFER_SIZE);
		
		//set reuse address on the socket acceptor as well
		acceptor.setReuseAddress(true);
		// close sessions when the acceptor is stopped
		acceptor.setCloseOnDeactivation(true);
		OrderedThreadPoolExecutor executor = new OrderedThreadPoolExecutor(Configuration.RTMP_WORKER_THREADS);
		executor.setThreadFactory(new CustomizableThreadFactory("RtmpWorkerExecutor-"));
		acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(executor));
		acceptor.bind(new InetSocketAddress(Configuration.RTMP_HOST, Configuration.RTMP_PORT));
		log.info("RTMP Socket Acceptor bound to :"+Configuration.RTMP_PORT);	
	}
	
	public void stop() {
		acceptor.unbind();
		log.info("RTMP Mina Transport stopped");
	}

	public void setIoHandler(IoHandler ioHandler) {
		this.ioHandler = ioHandler;
	}
}
