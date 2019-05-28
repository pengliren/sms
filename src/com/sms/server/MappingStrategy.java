package com.sms.server;

import com.sms.server.api.IMappingStrategy;

public class MappingStrategy implements IMappingStrategy {

	 /**
     *  Root constant
     */
	private static final String ROOT = "";
    /**
     *  Handler extension constant
     */
	private static final String HANDLER = ".handler";
    /**
     *  Dir separator constant
     */
	private static final String DIR = "/";
    /**
     *  Service extension constant
     */
	private static final String SERVICE = ".service";
    /**
     *  Default application name
     */
	private String defaultApp = "default";

    /**
     * Setter for default application name ('default' by default).
     * @param defaultApp     Default application
     */
	public void setDefaultApp(String defaultApp) {
		this.defaultApp = defaultApp;
	}

    /**
     * Resolves resource prefix from path. Default application used as root when path is specified
     * @param path          Path
     * @return              Resource prefix according to this naming strategy
     */
	public String mapResourcePrefix(String path) {
		if (path == null || path.equals(ROOT)) {
			return defaultApp + DIR;
		} else {
			return path + DIR;
		}
	}

    /**
     * Resolves scope handler name for path& Default application used as root when path is specified
     * @param path         Path
     * @return             Scope handler name according to this naming strategy
     */
	public String mapScopeHandlerName(String path) {
		if (path == null || path.equals(ROOT)) {
			return defaultApp + HANDLER;
		} else {
			return path + HANDLER;
		}
	}

    /**
     * Resolves service filename name from name
     * @param name      Service name
     * @return          Service filename according to this naming strategy
     */
	public String mapServiceName(String name) {
		return name + SERVICE;
	}
}
