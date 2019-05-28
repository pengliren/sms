package com.sms.server;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IClientRegistry;
import com.sms.server.api.IContext;
import com.sms.server.api.IGlobalScope;
import com.sms.server.api.IMappingStrategy;
import com.sms.server.api.IScope;
import com.sms.server.api.IScopeResolver;
import com.sms.server.api.persistence.IPersistenceStore;
import com.sms.server.api.service.IServiceInvoker;
import com.sms.server.service.ServiceInvoker;
import com.sms.server.util.FileUtil;

public class Context implements IContext {

	// Initialize Logging
	public static Logger logger = LoggerFactory.getLogger(Context.class);

	/**
	 * Spring application context
	 */
	//private ApplicationContext applicationContext;

	/**
	 * Core context
	 */
	//private BeanFactory coreContext;

	/**
	 * Context path
	 */
	private String contextPath = "";

	/**
	 * Scope resolver collaborator
	 */
	private IScopeResolver scopeResolver;

	/**
	 * Mapping stategy collaborator
	 */
	private IMappingStrategy mappingStrategy;

	/**
	 * Persistence store
	 */
	private IPersistenceStore persistanceStore;
	
	private ScopeContextBean ctxBean;

	/**
	 * Initializes core context bean factory using red5.core bean factory from
	 * red5.xml context
	 */
	@ConstructorProperties(value = { "" })
	public Context() {
	}

	/**
	 * Initializes app context and context path from given parameters
	 * 
	 * @param context Application context
	 * @param contextPath Context path
	 */
	@ConstructorProperties({"context", "contextPath"})
	public Context(/*ApplicationContext context,*/ String contextPath) {
		//setApplicationContext(context);
		this.contextPath = contextPath;
	}

	/**
	 * Return global scope
	 * 
	 * @return Global scope
	 */
	public IGlobalScope getGlobalScope() {
		IGlobalScope gs = scopeResolver.getGlobalScope();
		logger.trace("Global scope: {}", gs);
		return gs;
	}

	/**
	 * Return scope resolver
	 * 
	 * @return scope resolver
	 */
	public IScopeResolver getScopeResolver() {
		return scopeResolver;
	}

	/**
	 * Resolves scope using scope resolver collaborator
	 * 
	 * @param path Path to resolve
	 * @return Scope resolution result
	 */
	public IScope resolveScope(String path) {
		return scopeResolver.resolveScope(path);
	}

	/**
	 * Resolves scope from given root using scope resolver.
	 * 
	 * @param root Scope to start from.
	 * @param path Path to resolve.
	 * @return Scope resolution result.
	 */
	public IScope resolveScope(IScope root, String path) {
		return scopeResolver.resolveScope(root, path);
	}

	/**
	 * Setter for mapping strategy
	 * 
	 * @param mappingStrategy Mapping strategy
	 */
	public void setMappingStrategy(IMappingStrategy mappingStrategy) {
		this.mappingStrategy = mappingStrategy;
	}

	/**
	 * Setter for scope resolver
	 * 
	 * @param scopeResolver Scope resolver used to resolve scopes
	 */
	public void setScopeResolver(IScopeResolver scopeResolver) {
		this.scopeResolver = scopeResolver;
	}

	/**
	 * Return persistence store
	 * 
	 * @return Persistence store
	 */
	public IPersistenceStore getPersistanceStore() {
		return persistanceStore;
	}

	/**
	 * Setter for persistence store
	 * 
	 * @param persistanceStore Persistence store
	 */
	public void setPersistanceStore(IPersistenceStore persistanceStore) {
		this.persistanceStore = persistanceStore;
	}

	/**
	 * Setter for application context
	 * 
	 * @param context App context
	
	public void setApplicationContext(ApplicationContext context) {
		this.applicationContext = context;
		String deploymentType = System.getProperty("red5.deployment.type");
		logger.debug("Deployment type: " + deploymentType);
		if (deploymentType == null) {
			// standalone core context
			String config = System.getProperty("red5.conf_file");
			if (config == null) {
				config = "red5.xml";
			}
			coreContext = ContextSingletonBeanFactoryLocator.getInstance(config).useBeanFactory("red5.core")
					.getFactory();
		} else {
			logger.info("Setting parent bean factory as core");
			coreContext = applicationContext.getParentBeanFactory();
		}
	} */

	/**
	 * Return application context
	 * 
	 * @return App context
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	} */

	/**
	 * Setter for context path. Adds a slash at the end of path if there isn't  one
	 * 
	 * @param contextPath Context path
	 */
	public void setContextPath(String contextPath) {
		if (!contextPath.endsWith("/")) {
			contextPath += '/';
		}
		this.contextPath = contextPath;
	}
	
	public String getContextPath() {
		
		return this.contextPath;
	}

	/**
	 * Return client registry
	 * 
	 * @return Client registry
	 */
	public IClientRegistry getClientRegistry() {
		return ClientRegistry.getInstance();
	}

	/**
	 * Return scope
	 * 
	 * @return null
	 */
	public IScope getScope() {
		return null;
	}

	/**
	 * Return service invoker
	 * 
	 * @return Service invoker
	 */
	public IServiceInvoker getServiceInvoker() {
		return ServiceInvoker.getInstance();//serviceInvoker;
	}

	/**
	 * Look up service by name
	 * 
	 * @param serviceName Service name
	 * @return Service object
	 * @throws ServiceNotFoundException When service found but null
	 * @throws NoSuchBeanDefinitionException  When bean with given name doesn't exist
	 
	public Object lookupService(String serviceName) {
		serviceName = getMappingStrategy().mapServiceName(serviceName);
		try {
			Object bean = applicationContext.getBean(serviceName);
			if (bean != null) {
				return bean;
			} else {
				throw new ServiceNotFoundException(serviceName);
			}
		} catch (NoSuchBeanDefinitionException err) {
			throw new ServiceNotFoundException(serviceName);
		}
	}*/

	/**
	 * Look up scope handler for context path
	 * 
	 * @param contextPath Context path
	 * @return Scope handler
	 * @throws ScopeHandlerNotFoundException If there's no handler for given context path
	 
	public IScopeHandler lookupScopeHandler(String contextPath) {
		// Get target scope handler name
		String scopeHandlerName = getMappingStrategy().mapScopeHandlerName(contextPath);
		// Get bean from bean factory
		Object bean = applicationContext.getBean(scopeHandlerName);
		if (bean != null && bean instanceof IScopeHandler) {
			return (IScopeHandler) bean;
		} else {
			throw new ScopeHandlerNotFoundException(scopeHandlerName);
		}
	}*/

	/**
	 * Return mapping strategy used by this context. Mapping strategy define
	 * naming rules (prefixes, postfixes, default application name, etc) for all
	 * named objects in context.
	 * 
	 * @return Mapping strategy
	 */
	public IMappingStrategy getMappingStrategy() {
		return mappingStrategy;
	}

	/**
	 * Return array or resournce that match given pattern
	 * 
	 * @param pattern Pattern to check against
	 * @return Array of Resource objects
	 * @throws IOException On I/O exception
	 * 
	 * @see org.springframework.core.io.Resource
	*/ 
	public File[] getResources(String pattern) {
		
		List<File> fileList = FileUtil.getFileList(contextPath +"/"+ pattern);
		return (File[])fileList.toArray();
	}

	/**
	 * Return resouce by path
	 * 
	 * @param path Resource path
	 * @return Resource
	 * 
	 * @see org.springframework.core.io.Resource
	*/
	public File getResource(String path) {
		
		File file = new File(contextPath +"/"+ path);
		return file;
	}

	/**
	 * Resolve scope from host and path
	 * 
	 * @param host Host
	 * @param path Path
	 * @return Scope
	 * 
	 * @see org.red5.server.api.IScope
	 * @see org.red5.server.Scope
	 */
	public IScope resolveScope(String host, String path) {
		return scopeResolver.resolveScope(path);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasBean(String beanId) {
		
		return ctxBean.getClazz(ScopeContextBean.RTMPSAMPLEACCESS_BEAN) != null;
	}

	/**
	 * Return bean instantiated by bean factory
	 * 
	 * @param beanId Bean name
	 * @return Instantiated bean
	 * 
	 * @see org.springframework.beans.factory.BeanFactory
	*/
	public Object getBean(String beanId) {
		// for war applications the "application" beans are not stored in the
		// sub-contexts, so look in the application context first and the core
		// context second
		Object bean = null;
		ContextBean contextBean = ctxBean.getClazz(beanId);
		Map<String, String> proMap = contextBean.getPropertyMap();
		try {
			bean = Class.forName(contextBean.getClassName()).newInstance();
			for(String key : proMap.keySet()) {
				BeanUtils.setProperty(bean, key, proMap.get(key));
			}
		} catch (Exception e) {
			logger.error("Bean not found in context {} : {}", beanId, e.getMessage());
		}
		return bean;
	} 
	
	public Object getService(String clazz) {
		
		ContextBean contextBean = ctxBean.getClazz(clazz);
		if(contextBean == null) return null;
		Map<String, String> proMap = contextBean.getPropertyMap();
		Class<?> bean = null;
		Object servicBean = null;
		try {
			bean = Class.forName(contextBean.getClassName());
			Method method = bean.getMethod("getInstance");
			servicBean = method.invoke(bean, (Object[]) null);
			
			for(String key : proMap.keySet()) {
				
				BeanUtils.setProperty(servicBean, key, proMap.get(key));
			}
			
		} catch (Exception e) {
			logger.error("Bean not found in context {} : {}", clazz, e);
		}
		return servicBean;
	}
	
	public void setBean(ScopeContextBean ctxBean) {
		
		this.ctxBean = ctxBean;
	}
	
	public ScopeContextBean getScopeCtxBean() {
		
		return this.ctxBean;
	}

	/**
	 * Return core Red5 se rvice instantiated by core context bean factory
	 * 
	 * @param beanId Bean name
	 * @return Core Red5 service instantiated
	 * 
	 * @see org.springframework.beans.factory.BeanFactory
	 
	public Object getCoreService(String beanId) {
		return coreContext.getBean(beanId);
	}

	public void setCoreBeanFactory(BeanFactory core) {
		coreContext = core;
	}*/

	/**
	 * Return current thread's context classloader
	 * 
	 * @return Classloder context of current thread
	 
	public ClassLoader getClassLoader() {
		return applicationContext.getClassLoader();
	}*/

	/**
	 * Allows for reconstruction via CompositeData.
	 * 
	 * @param cd composite data
	 * @return Context class instance
	 */
	public static Context from(CompositeData cd) {
		Context instance = new Context();
		if (cd.containsKey("context") && cd.containsKey("contextPath")) {
			Object context = cd.get("context");
			Object contextPath = cd.get("contextPath");
			if (context != null && contextPath != null) {
				instance = new Context(/*(ApplicationContext) context,*/ (String) contextPath);
			}
		}
		return instance;
	}
}
