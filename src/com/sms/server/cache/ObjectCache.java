package com.sms.server.cache;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.util.SystemTimer;

/**
 * Cache Object
 * @author pengliren
 *
 */
public class ObjectCache {

	private static final Logger log = LoggerFactory.getLogger(CacheManager.class);
	
	private static int DEFAULT_EXPIRE_TIME = 1200;
	
	private class CacheItem {
		private int expire = DEFAULT_EXPIRE_TIME;
		private long accessTime = 0;
		private Object object = null;

		public CacheItem(Object object) {
			this.object = object;
			this.accessTime = SystemTimer.currentTimeMillis();
		}

		public CacheItem(Object object, int expire) {
			this.object = object;
			this.expire = expire;
			this.accessTime = SystemTimer.currentTimeMillis();
		}

		public void access() {
			accessTime = SystemTimer.currentTimeMillis();
		}

		public boolean isExpired() {
			if (expire == -1)
				return false;
			else
				return (accessTime + expire * 1000) < SystemTimer.currentTimeMillis();
		}

		public Object getObject() {
			return object;
		}

	}
	
	private ConcurrentHashMap<String, CacheItem> items = new ConcurrentHashMap<String, CacheItem>();

	public ObjectCache() {
	}
	
	public void collect() {
		// check all cache item
		Iterator<String> it = items.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			CacheItem item = items.get(key);

			// timeout
			if (item.isExpired()) {
				log.debug("cache {} isExpired and remove!", key);
				it.remove();
			}
		}

	}
	
	public Object get(String key) {
		Object obj = null;
		if (items.containsKey(key)) {
			CacheItem item = items.get(key);
			// check for timeout
			if (!item.isExpired()) {
				item.access();
				obj = item.getObject();
			} else {
				items.remove(key);
			}
		}

		return obj;
	}
	
	public Set<String> getKeys() {
		
		return items.keySet();
	}

	public void put(String key, Object obj) {
		items.put(key, new CacheItem(obj));
	}

	public void put(String key, Object obj, int expire) {
		items.put(key, new CacheItem(obj, expire));
	}

	public void remove(String key) {
		items.remove(key);
	}
	
	public void removeAll() {
		
		items.clear();
	}
	
	public boolean isKeyInCache(String key) {
		
		for(String temp : items.keySet()) {
			if(temp.equals(key)) return true;
		}
		return false;
	}
}
