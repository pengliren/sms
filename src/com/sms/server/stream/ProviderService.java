package com.sms.server.stream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.io.IStreamableFileFactory;
import com.sms.io.IStreamableFileService;
import com.sms.io.StreamableFileFactory;
import com.sms.server.BasicScope;
import com.sms.server.Scope;
import com.sms.server.api.IBasicScope;
import com.sms.server.api.IScope;
import com.sms.server.api.stream.IBroadcastStream;
import com.sms.server.api.stream.IStreamFilenameGenerator;
import com.sms.server.api.stream.IStreamFilenameGenerator.GenerationType;
import com.sms.server.messaging.IMessageInput;
import com.sms.server.messaging.IPipe;
import com.sms.server.messaging.InMemoryPullPullPipe;
import com.sms.server.stream.provider.FileProvider;

public class ProviderService implements IProviderService {

	private static final Logger log = LoggerFactory.getLogger(ProviderService.class);
	
	private static final class SingletonHolder {

		private static final ProviderService INSTANCE = new ProviderService();
	}

	protected ProviderService() {
		
	}
	
	public static ProviderService getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	/** {@inheritDoc} */
	public INPUT_TYPE lookupProviderInput(IScope scope, String name, int type) {
		INPUT_TYPE result = INPUT_TYPE.NOT_FOUND;
		if (scope.getBasicScope(IBroadcastScope.TYPE, name) != null) {
			//we have live input
			result = INPUT_TYPE.LIVE;
		} else {
            //"default" to VOD as a missing file will be picked up later on 
		 	result = INPUT_TYPE.VOD;  			
		 	File file = null;
			try {
				file = getStreamFile(scope, name);
				if (file == null) {
					if(type == -2) {
						result = INPUT_TYPE.LIVE_WAIT;
					}
					log.debug("Requested stream: {} does not appear to be of VOD type", name);
				}
			} catch (IOException e) {
				log.warn("Exception attempting to lookup file: {}", name, e);
				log.warn("Exception {}", e);
			} finally {
				//null it to prevent leak or file locking
				file = null;				
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	public IMessageInput getProviderInput(IScope scope, String name) {
		IMessageInput msgIn = getLiveProviderInput(scope, name, false);
		if (msgIn == null) {
			return getVODProviderInput(scope, name);
		}
		return msgIn;
	}

	/** {@inheritDoc} */
	public IMessageInput getLiveProviderInput(IScope scope, String name, boolean needCreate) {
		log.debug("Get live provider input for {} scope: {}", name, scope);
		if (log.isDebugEnabled()) {
			((Scope) scope).dump();
		}
		//make sure the create is actually needed
		IBasicScope basicScope = scope.getBasicScope(IBroadcastScope.TYPE, name);
		if (basicScope == null) {
			if (needCreate) {
				synchronized(scope) {
					// Re-check if another thread already created the scope
					basicScope = scope.getBasicScope(IBroadcastScope.TYPE, name);
					if (basicScope == null) {
						basicScope = new BroadcastScope(scope, name);
						scope.addChildScope(basicScope);
					}
				}
			} else {
				return null;
			}
		}
		if (!(basicScope instanceof IBroadcastScope)) {
			return null;
		}
		return (IBroadcastScope) basicScope;
	}

	/** {@inheritDoc} */
	public IMessageInput getVODProviderInput(IScope scope, String name) {
		log.debug("getVODProviderInput - scope: {} name: {}", scope, name);
		File file = getVODProviderFile(scope, name);
		if (file == null) {
			return null;
		}
		IPipe pipe = new InMemoryPullPullPipe();
		pipe.subscribe(new FileProvider(scope, file), null);
		return pipe;
	}

	/** {@inheritDoc} */
	public File getVODProviderFile(IScope scope, String name) {
		log.debug("getVODProviderFile - scope: {} name: {}", scope, name);
		File file = null;
		try {
			log.trace("getVODProviderFile scope path: {} name: {}", scope.getContextPath(), name);
			file = getStreamFile(scope, name);
		} catch (IOException e) {
			log.error("Problem getting file: {}", name, e);
		}
		if (file == null || !file.exists()) {
			//if there is no file extension this is most likely a live stream
			if (name.indexOf('.') > 0) {
				log.info("File was null or did not exist: {}", name);
			} else {
				log.trace("VOD file {} was not found, may be live stream", name);
			}
			return null;
		}
		return file;
	}

	/** {@inheritDoc} */
	public boolean registerBroadcastStream(IScope scope, String name, IBroadcastStream bs) {
		log.debug("Registering - name: {} stream: {} scope: {}", new Object[] { name, bs, scope });
		if (log.isDebugEnabled()) {
			((Scope) scope).dump();
		}
		boolean status = false;
		IBasicScope basicScope = scope.getBasicScope(IBroadcastScope.TYPE, name);
		if (basicScope == null) {
			log.debug("Creating a new scope");
			basicScope = new BroadcastScope(scope, name);
			if (scope.addChildScope(basicScope)) {
				log.debug("Broadcast scope added");
			} else {
				log.warn("Broadcast scope was not added to {}", scope);
			}
		}
		if (basicScope instanceof IBroadcastScope) {
			log.debug("Subscribing scope {} to provider {}", basicScope, bs.getProvider());
			status = ((IBroadcastScope) basicScope).subscribe(bs.getProvider(), null);
		}
		return status;
	}

	/** {@inheritDoc} */
	public List<String> getBroadcastStreamNames(IScope scope) {
		// TODO: return result of "getBasicScopeNames" when the api has
		// changed to not return iterators
		List<String> result = new ArrayList<String>();
		Iterator<String> it = scope.getBasicScopeNames(IBroadcastScope.TYPE);
		while (it.hasNext()) {
			result.add(it.next());
		}
		it = null;
		return result;
	}

	/** {@inheritDoc} */
	public boolean unregisterBroadcastStream(IScope scope, String name) {
		return unregisterBroadcastStream(scope, name, null);
	}

	/** {@inheritDoc} */
	public boolean unregisterBroadcastStream(IScope scope, String name, IBroadcastStream bs) {
		log.debug("Unregistering - name: {} stream: {} scope: {}", new Object[] { name, bs, scope });
		if (log.isDebugEnabled()) {
			((Scope) scope).dump();
		}
		boolean status = false;
		IBasicScope basicScope = scope.getBasicScope(IBroadcastScope.TYPE, name);
		if (basicScope instanceof IBroadcastScope) {
			if (bs != null) {
				log.debug("Unsubscribing scope {} from provider {}", basicScope, bs.getProvider());
				((IBroadcastScope) basicScope).unsubscribe(bs.getProvider());
			}
			//if the scope has no listeners try to remove it
			if (!((BasicScope) basicScope).hasEventListeners()) {
				log.debug("Scope has no event listeners attempting removal");
				scope.removeChildScope(basicScope);
			}
			if (log.isDebugEnabled()) {
				//verify that scope was removed
				if (scope.getBasicScope(IBroadcastScope.TYPE, name) == null) {
					log.debug("Scope was removed");
				} else {
					log.debug("Scope was not removed");
				}
			}
			status = true;
		}
		return status;
	}

	private File getStreamFile(IScope scope, String name) throws IOException {
		IStreamableFileFactory factory = StreamableFileFactory.getInstance();//(IStreamableFileFactory) ScopeUtils.getScopeService(scope,
				//IStreamableFileFactory.class);
		if (name.indexOf(':') == -1 && name.indexOf('.') == -1) {
			// Default to .flv files if no prefix and no extension is given.
			name = "flv:" + name;
		}
		log.debug("getStreamFile null check - factory: {} name: {}", factory, name);
		for (IStreamableFileService service : factory.getServices()) {
			if (name.startsWith(service.getPrefix() + ':')) {
				name = service.prepareFilename(name);
				break;
			}
		}

		IStreamFilenameGenerator filenameGenerator = DefaultStreamFilenameGenerator.getInstance();
		String filename = filenameGenerator.generateFilename(scope, name, GenerationType.PLAYBACK);
		File file = null;
		//most likely case first
		if (!filenameGenerator.resolvesToAbsolutePath()) {
			file = scope.getContext().getResource(filename);
		} else {
			file = new File(filename);
		}
		//check files existence
		if (file != null && !file.exists()) {
			//if it does not exist then null it out
			file = null;
		}
		return file;

	}

}
