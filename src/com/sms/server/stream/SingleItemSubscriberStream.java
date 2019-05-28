package com.sms.server.stream;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.ScopeContextBean;
import com.sms.server.api.IConnection;
import com.sms.server.api.IScope;
import com.sms.server.api.SMS;
import com.sms.server.api.scheduling.ISchedulingService;
import com.sms.server.api.stream.IPlayItem;
import com.sms.server.api.stream.ISingleItemSubscriberStream;
import com.sms.server.api.stream.IStreamAwareScopeHandler;
import com.sms.server.api.stream.OperationNotSupportedException;
import com.sms.server.api.stream.StreamState;
import com.sms.server.scheduling.QuartzSchedulingService;
import com.sms.server.util.CustomizableThreadFactory;
import com.sms.server.util.SystemTimer;

/**
 * Stream of a single play item for a subscriber
 */
public class SingleItemSubscriberStream extends AbstractClientStream implements ISingleItemSubscriberStream {

	private static final Logger log = LoggerFactory.getLogger(SingleItemSubscriberStream.class);

	/**
	 * Executor that will be used to schedule stream playback to keep
	 * the client buffer filled.
	 */
	protected static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10, new CustomizableThreadFactory("SingleItemSubscriberStreamExecutor-"));	
	
	/**
	 * Interval in ms to check for buffer underruns in VOD streams.
	 */
	protected int bufferCheckInterval = 0;

	/**
	 * Number of pending messages at which a <code>NetStream.Play.InsufficientBW</code>
	 * message is generated for VOD streams.
	 */
	protected int underrunTrigger = 10;

	/**
	 * Timestamp this stream was created.
	 */
	protected long creationTime = SystemTimer.currentTimeMillis();
	
	private volatile IPlayItem item;

	/**
	 * Plays items back
	 */
	protected PlayEngine engine;

	public void setPlayItem(IPlayItem item) {
		this.item = item;
	}

	public void play() throws IOException {
		try {
			engine.play(item);
		} catch (StreamNotFoundException e) {
			//TODO send stream not found to subscriber
			throw new IOException(e);
		}
	}

	/** {@inheritDoc} */
	public void pause(int position) {
		try {
			engine.pause(position);
		} catch (IllegalStateException e) {
			log.debug("pause caught an IllegalStateException");
		}
	}

	/** {@inheritDoc} */
	public void resume(int position) {
		try {
			engine.resume(position);
		} catch (IllegalStateException e) {
			log.debug("resume caught an IllegalStateException");
		}
	}

	/** {@inheritDoc} */
	public void stop() {
		try {
			engine.stop();
		} catch (IllegalStateException e) {
			log.debug("stop caught an IllegalStateException");
		}
	}

	/** {@inheritDoc} */
	public void seek(int position) throws OperationNotSupportedException {
		try {
			engine.seek(position);
		} catch (IllegalStateException e) {
			log.debug("seek caught an IllegalStateException");
		}
	}

	public boolean isPaused() {
		return state == StreamState.PAUSED;
	}

	/** {@inheritDoc} */
	public void receiveVideo(boolean receive) {
		boolean receiveVideo = engine.receiveVideo(receive);
		if (!receiveVideo && receive) {
			//video has been re-enabled
			seekToCurrentPlayback();
		}
	}

	/** {@inheritDoc} */
	public void receiveAudio(boolean receive) {
		//check if engine currently receives audio, returns previous value
		boolean receiveAudio = engine.receiveAudio(receive);
		if (receiveAudio && !receive) {
			//send a blank audio packet to reset the player
			engine.sendBlankAudio(true);
		} else if (!receiveAudio && receive) {
			//do a seek	
			seekToCurrentPlayback();
		}
	}

	/** {@inheritDoc} */
	@Override
	public StreamState getState() {
		return state;
	}

	/** {@inheritDoc} */
	@Override
	public void setState(StreamState state) {
		this.state = state;
	}

	/**
	 * Creates a play engine based on current services (scheduling service, consumer service, and provider service).
	 * This method is useful during unit testing.
	 */
	PlayEngine createEngine(ISchedulingService schedulingService, IConsumerService consumerService,
			IProviderService providerService) {
		engine = new PlayEngine.Builder(this, schedulingService, consumerService, providerService).build();
		return engine;
	}

	/**
	 * Set the executor to use.
	 * 
	 * @param executor the executor
	 */
	public void setExecutor(ScheduledThreadPoolExecutor executor) {
		PlaylistSubscriberStream.executor = executor;
	}

	/**
	 * Return the executor to use.
	 * 
	 * @return the executor
	 */
	public ScheduledThreadPoolExecutor getExecutor() {
		if (executor == null) {
			log.warn("ScheduledThreadPoolExecutor was null on request");
		}
		return executor;
	}

	/**
	 * Set interval to check for buffer underruns. Set to <code>0</code> to
	 * disable.
	 * 
	 * @param bufferCheckInterval interval in ms
	 */
	public void setBufferCheckInterval(int bufferCheckInterval) {
		this.bufferCheckInterval = bufferCheckInterval;
	}

	/**
	 * Set maximum number of pending messages at which a
	 * <code>NetStream.Play.InsufficientBW</code> message will be
	 * generated for VOD streams
	 * 
	 * @param underrunTrigger the maximum number of pending messages
	 */
	public void setUnderrunTrigger(int underrunTrigger) {
		this.underrunTrigger = underrunTrigger;
	}	
	
	public void start() {
		//ensure the play engine exists
		if (engine == null) {
			IScope scope = getScope();
			if (scope != null) {
				//IContext ctx = scope.getContext();
				ISchedulingService schedulingService = null;
				schedulingService = QuartzSchedulingService.getInstance();
				IConsumerService consumerService = (IConsumerService) getScope().getContext().getService(ScopeContextBean.CONSUMERSERVICE_BEAN);
				IProviderService providerService = (IProviderService) scope.getContext().getService(ScopeContextBean.PROVIDERSERVICE_BEAN);
				engine = new PlayEngine.Builder(this, schedulingService, consumerService, providerService).build();
			} else {
				log.error("Scope was null on start");
			}
		}
		//set buffer check interval
		engine.setBufferCheckInterval(bufferCheckInterval);
		//set underrun trigger
		engine.setUnderrunTrigger(underrunTrigger);
		// Start playback engine
		engine.start();
		// Notify subscribers on start
		onChange(StreamState.STARTED);
	}

	public void close() {
		engine.close();
		onChange(StreamState.CLOSED);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void onChange(final StreamState state, final Object... changed) {
		Notifier notifier = null;
		IStreamAwareScopeHandler handler = getStreamAwareHandler();
		switch (state) {
			case SEEK:
				//notifies subscribers on seek
				if (handler != null) {
					notifier = new Notifier(this, handler) {
						public void run() {
							//make sure those notified have the correct connection
							SMS.setConnectionLocal(conn);
							//get item being played
							IPlayItem item = (IPlayItem) changed[0];
							//seek position
							int position = (Integer) changed[1];
							try {
								handler.streamPlayItemSeek(stream, item, position);
							} catch (Throwable t) {
								log.error("error notify streamPlayItemSeek", t);
							}
							// clear thread local reference
							SMS.setConnectionLocal(null);
						}
					};
				}
				break;
			case PAUSED:
				//set the paused state
				this.setState(StreamState.PAUSED);
				//notifies subscribers on pause
				if (handler != null) {
					notifier = new Notifier(this, handler) {
						public void run() {
							//make sure those notified have the correct connection
							SMS.setConnectionLocal(conn);
							//get item being played
							IPlayItem item = (IPlayItem) changed[0];
							//playback position
							int position = (Integer) changed[1];
							try {
								handler.streamPlayItemPause(stream, item, position);
							} catch (Throwable t) {
								log.error("error notify streamPlayItemPause", t);
							}
							// clear thread local reference
							SMS.setConnectionLocal(null);
						}
					};
				}
				break;
			case RESUMED:
				//resume playing
				this.setState(StreamState.PLAYING);
				//notifies subscribers on resume
				if (handler != null) {
					notifier = new Notifier(this, handler) {
						public void run() {
							//make sure those notified have the correct connection
							SMS.setConnectionLocal(conn);
							//get item being played
							IPlayItem item = (IPlayItem) changed[0];
							//playback position
							int position = (Integer) changed[1];
							try {
								handler.streamPlayItemResume(stream, item, position);
							} catch (Throwable t) {
								log.error("error notify streamPlayItemResume", t);
							}
							// clear thread local reference
							SMS.setConnectionLocal(null);
						}
					};
				}
				break;
			case PLAYING:
				//notifies subscribers on play
				if (handler != null) {
					notifier = new Notifier(this, handler) {
						public void run() {
							//make sure those notified have the correct connection
							SMS.setConnectionLocal(conn);
							//get item being played
							IPlayItem item = (IPlayItem) changed[0];
							//is it a live broadcast
							boolean isLive = (Boolean) changed[1];
							try {
								handler.streamPlayItemPlay(stream, item, isLive);
							} catch (Throwable t) {
								log.error("error notify streamPlayItemPlay", t);
							}
							// clear thread local reference
							SMS.setConnectionLocal(null);
						}
					};
				}
				break;
			case CLOSED:
				//notifies subscribers on close
				if (handler != null) {
					notifier = new Notifier(this, handler) {
						public void run() {
							//make sure those notified have the correct connection
							SMS.setConnectionLocal(conn);
							try {
								handler.streamSubscriberClose(stream);
							} catch (Throwable t) {
								log.error("error notify streamSubscriberClose", t);
							}
							// clear thread local reference
							SMS.setConnectionLocal(null);
						}
					};
				}
				break;
			case STARTED:
				//notifies subscribers on start
				if (handler != null) {
					notifier = new Notifier(this, handler) {
						public void run() {
							//make sure those notified have the correct connection
							SMS.setConnectionLocal(conn);
							try {
								handler.streamSubscriberStart(stream);
							} catch (Throwable t) {
								log.error("error notify streamSubscriberStart", t);
							}
							// clear thread local reference
							SMS.setConnectionLocal(null);
						}
					};
				}
				break;
			case STOPPED:
				//set the stopped state
				this.setState(StreamState.STOPPED);
				//notifies subscribers on stop
				if (handler != null) {
					notifier = new Notifier(this, handler) {
						public void run() {
							//make sure those notified have the correct connection
							SMS.setConnectionLocal(conn);
							//get the item that was stopped
							IPlayItem item = (IPlayItem) changed[0];
							try {
								handler.streamPlayItemStop(stream, item);
							} catch (Throwable t) {
								log.error("error notify streamPlaylistItemStop", t);
							}
							// clear thread local reference
							SMS.setConnectionLocal(null);
						}
					};
				}
				break;
			case END:
				//notified by the play engine when the current item reaches the end
				break;
			default:
				//there is no "default" handling
		}
		if (notifier != null) {
			executor.execute(notifier);
		}
	}

	/**
	 * Seek to current position to restart playback with audio and/or video.
	 */
	private void seekToCurrentPlayback() {
		if (engine.isPullMode()) {
			try {
				// TODO: figure out if this is the correct position to seek to
				final long delta = System.currentTimeMillis() - engine.getPlaybackStart();
				engine.seek((int) delta);
			} catch (OperationNotSupportedException err) {
				// Ignore error, should not happen for pullMode engines
			}
		}
	}

	/**
	 * Handles notifications in a separate thread.
	 */
	public class Notifier implements Runnable {

		ISingleItemSubscriberStream stream;

		IStreamAwareScopeHandler handler;
		
		IConnection conn;

		public Notifier(ISingleItemSubscriberStream stream, IStreamAwareScopeHandler handler) {
			log.trace("Notifier - stream: {} handler: {}", stream, handler);
			this.stream = stream;
			this.handler = handler;
			this.conn = stream.getConnection();
		}

		public void run() {
		}

	}	
	
}
