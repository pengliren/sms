package com.sms.server.api;

import java.io.File;

import com.sms.server.ScopeContextBean;
import com.sms.server.api.persistence.IPersistenceStore;
import com.sms.server.api.service.IServiceInvoker;


public interface IContext {

	public static final String ID = "sms.context";

	// public IScopeResolver getScopeResolver();
	/**
	 * Get client registry. Client registry is a place where all clients are
	 * registred.
	 * 
	 * @return	Client registry object
	 */
	IClientRegistry getClientRegistry();

	/**
	 * Returns service invoker object. Service invokers are objects that make
	 * service calls to client side NetConnection objects.
	 * 
	 * @return		Service invoker object
	 */
	IServiceInvoker getServiceInvoker();

	/**
	 * Returns persistence store object, a storage for persistent objects like
	 * persistent SharedObjects.
	 * 
	 * @return	Persistence store object
	 */
	IPersistenceStore getPersistanceStore();

	/**
	 * Returns scope handler (object that handle all actions related to the
	 * scope) by path. See {@link IScopeHandler} for details.
	 * 
	 * @param path
	 *            Path of scope handler
	 * @return		Scope handler
	
	IScopeHandler lookupScopeHandler(String path);
 */
	/**
	 * Returns scope by path. You can think of IScope as of tree items, used to
	 * separate context and resources between users. See {@link IScope} for more
	 * details.
	 * 
	 * @param path
	 *            Path of scope
	 * @return		IScope object
	 */
	IScope resolveScope(String path);

	/**
	 * Returns scope by path from given root. You can think of IScope as of tree
	 * items, used to separate context and resources between users.
	 * See {@link IScope} for more details.
	 * 
	 * @param root
	 *            Root to start from
	 * @param path
	 *            Path of scope
	 * @return		IScope object
	 */
	IScope resolveScope(IScope root, String path);

	/**
	 * Returns global scope reference
	 * 
	 * @return	global scope reference
	 */
	IGlobalScope getGlobalScope();

	/**
	 * Returns service by name. 
	 * 
	 * @param serviceName
	 *            Name of service
	 * @return				Service object
	 
	Object lookupService(String serviceName);
*/
	/**
	 * Returns bean by ID
	 * 
	 * @param beanId
	 *            Bean ID
	 * @return			Given bean instance
	 */
	Object getBean(String beanId);
	ScopeContextBean getScopeCtxBean();
	void setBean(ScopeContextBean ctxBean);

	Object getService(String clazz);
	/**
	 * Returns true if the context contains a certain bean,
	 * false otherwise.
	 * @param beanId	The name of the bean to find. 
	 * @return	True if the bean exists, false otherwise. 
	 */
	boolean hasBean(String beanId);

	/**
	 * Returns core service by bean id
	 * 
	 * @param beanId
	 *            Bean ID
	 * @return			Core service
	 
	Object getCoreService(String beanId);
*/
	public File[] getResources(String pattern);

	
	public File getResource(String path);
	
	public String getContextPath();
	
	/**
	 * Returns IMappingStrategy object
	 * 
	 * @return	IMappingStrategy object
	 */
	public IMappingStrategy getMappingStrategy();
}
