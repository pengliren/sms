package com.sms.server.net.udp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sms.server.Configuration;

/**
 * UDP Port Manager
 * UDP Port auto get
 * UDP Port auto recycle
 * @author pengliren
 *
 */
public class UDPPortManager {

	private int portRecycleTime = 600000;
	private int nextPort = Configuration.UDP_PORT_START;
	private Map<Integer, AvailablePortHolder> availablePorts = new HashMap<Integer, UDPPortManager.AvailablePortHolder>();
	private List<Integer> usedPorts = new ArrayList<Integer>();
	private List<Integer> portByNumberPorts = new ArrayList<Integer>();
	private Object portLock = new Object();
	
	private static final class SingletonHolder {

		private static final UDPPortManager INSTANCE = new UDPPortManager();
	}
	
	private UDPPortManager() {
		
	}

	public static UDPPortManager getInstance() {

		return SingletonHolder.INSTANCE;
	}
	
	public int getDatagramStartingPort() {
		return this.nextPort;
	}
	
	public int getPortRecycleTime() {
		return this.portRecycleTime;
	}

	public void setPortRecycleTime(int paramInt) {
		this.portRecycleTime = paramInt;
	}
	
	public int[] expandToPortPair(int port) {
		int[] pair = new int[2];
		pair[0] = -1;
		pair[1] = -1;
		if (port % 2 == 0) {
			pair[0] = port;
			pair[1] = (port + 1);
		} else {
			pair[0] = (port - 1);
			pair[1] = (port + 2);
		}
		return pair;
	}

	public boolean isPortAutoAssigned(int port) {
		if (port % 2 > 0)
			port--;
		Integer localInteger = new Integer(port);
		synchronized (this.portLock) {
			return !this.portByNumberPorts.contains(localInteger);
		}
	}

	public int acquireUDPPortPair(int port) {
		int i = -1;
		synchronized (this.portLock) {
			if (port % 2 == 0) {
				Integer localInteger1 = new Integer(port);
				if (!this.usedPorts.contains(Integer.valueOf(port))) {
					i = port;
					this.usedPorts.add(Integer.valueOf(i));
					this.portByNumberPorts.add(Integer.valueOf(i));
					if (this.availablePorts.containsKey(localInteger1))
						this.availablePorts.remove(localInteger1);
				}
			} else {
				int j = port - 1;
				int k = port + 1;
				Integer localInteger2 = new Integer(j);
				Integer localInteger3 = new Integer(k);
				if ((!this.usedPorts.contains(Integer.valueOf(j))) && (!this.usedPorts.contains(Integer.valueOf(k)))) {
					i = port;
					this.usedPorts.add(Integer.valueOf(j));
					this.usedPorts.add(Integer.valueOf(k));
					this.portByNumberPorts.add(Integer.valueOf(j));
					this.portByNumberPorts.add(Integer.valueOf(k));
					if (this.availablePorts.containsKey(localInteger2))
						this.availablePorts.remove(localInteger2);
					if (this.availablePorts.containsKey(localInteger3))
						this.availablePorts.remove(localInteger3);
				}
			}
		}
		return i;
	}

	public int acquireUDPPortPair() {
		int i = -1;
		synchronized (this.portLock) {
			if (this.availablePorts.size() > 0) {
				long ts = System.currentTimeMillis();
				Iterator<Integer> localIterator = this.availablePorts.keySet().iterator();
				while (localIterator.hasNext()) {
					Integer localInteger = localIterator.next();
					int j = localInteger.intValue();
					AvailablePortHolder localAvailablePortHolder = (AvailablePortHolder) this.availablePorts.get(localInteger);
					if (localAvailablePortHolder.canRecycle(ts)) {
						i = j;
						break;
					}
				}
				if (i >= 0)
					this.availablePorts.remove(new Integer(i));
			}
			if (i < 0) {
				while (true) {
					i = this.nextPort;
					this.nextPort += 2;
					if (!this.usedPorts.contains(Integer.valueOf(i)))
						break;
				}
				this.usedPorts.add(Integer.valueOf(i));
			}
		}
		return i;
	}

	public void releaseUDPPortPair(int port) {
		synchronized (this.portLock) {
			if (port % 2 == 0) {
				Integer localInteger1 = new Integer(port);
				if (!this.portByNumberPorts.contains(localInteger1))
					this.availablePorts.put(localInteger1, new AvailablePortHolder(port));
				this.usedPorts.remove(localInteger1);
				this.portByNumberPorts.remove(localInteger1);
			} else {
				int i = port - 1;
				int j = port + 1;
				if ((!this.usedPorts.contains(Integer.valueOf(i))) && (!this.usedPorts.contains(Integer.valueOf(j)))) {
					Integer localInteger2 = new Integer(i);
					Integer localInteger3 = new Integer(j);
					if (!this.portByNumberPorts.contains(localInteger2))
						this.availablePorts.put(localInteger2, new AvailablePortHolder(i));
					if (!this.portByNumberPorts.contains(localInteger3))
						this.availablePorts.put(localInteger3, new AvailablePortHolder(j));
					this.usedPorts.remove(localInteger2);
					this.usedPorts.remove(localInteger3);
					this.portByNumberPorts.remove(localInteger2);
					this.portByNumberPorts.remove(localInteger3);
				}
			}
		}
	}
	  
	class AvailablePortHolder {
		public int port;
		public long timecode = -1L;

		public AvailablePortHolder(int port) {
			this.port = port;
			this.timecode = System.currentTimeMillis();
		}

		public boolean canRecycle(long time) {
			return time - this.timecode >= UDPPortManager.this.portRecycleTime;
		}
	}
}
