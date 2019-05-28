package com.sms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sms.server.api.IBasicScope;
import com.sms.server.api.IClient;
import com.sms.server.api.IClientRegistry;
import com.sms.server.api.IConnection;
import com.sms.server.api.IContext;
import com.sms.server.api.IScope;
import com.sms.server.api.IScopeHandler;
import com.sms.server.api.SMS;
import com.sms.server.api.event.IEvent;
import com.sms.server.api.service.IServiceCall;

public class CoreHandler implements IScopeHandler {

	protected static Logger log = LoggerFactory.getLogger(CoreHandler.class);

	/** {@inheritDoc} */
    public boolean addChildScope(IBasicScope scope) {
		return true;
	}

    /**
     * Connects client to the scope
     *
     * @param conn                 Client conneciton
     * @param scope                Scope
     * @return                     true if client was registred within scope, false otherwise
     */
    public boolean connect(IConnection conn, IScope scope) {
		return connect(conn, scope, null);
	}

    /**
     * Connects client to the scope
     *
     * @param conn                  Client connection
     * @param scope                 Scope
     * @param params                Params passed from client side with connect call
     * @return                      true if client was registered within scope, false otherwise
     */
    public boolean connect(IConnection conn, IScope scope, Object[] params) {
		log.debug("Connect to core handler ?");
		boolean connect = false;
		
        // Get session id
        String id = conn.getSessionId();
        log.trace("Session id: {}", id);

		// Use client registry from scope the client connected to.
		IScope connectionScope = SMS.getConnectionLocal().getScope();
		log.debug("Connection scope: {}", (connectionScope == null ? "is null" : "not null"));

        // when the scope is null bad things seem to happen, if a null scope is OK then 
        // this block will need to be removed - Paul
        if (connectionScope != null) {
            // Get client registry for connection scope
            IClientRegistry clientRegistry = connectionScope.getContext().getClientRegistry();
    		log.debug("Client registry: {}", (clientRegistry == null ? "is null" : "not null"));
    		if (clientRegistry != null) {
                // Get client from registry by id or create a new one
                IClient client = clientRegistry.hasClient(id) ? clientRegistry.lookupClient(id) : clientRegistry.newClient(params);    
        		// We have a context, and a client object.. time to init the connection.
        		conn.initialize(client);
        		// we could checked for banned clients here 
        		connect = true;
    		} else {
        		log.error("No client registry was found, clients cannot be looked-up or created");    			
    		}
        } else {
    		log.error("No connection scope was found");        	
        }

        return connect;
	}

	/** {@inheritDoc} */
    public void disconnect(IConnection conn, IScope scope) {
		// do nothing here
	}

	/** {@inheritDoc} */
    public boolean join(IClient client, IScope scope) {
		return true;
	}

	/** {@inheritDoc} */
    public void leave(IClient client, IScope scope) {
		// do nothing here
	}

	/** {@inheritDoc} */
    public void removeChildScope(IBasicScope scope) {
		// do nothing here
	}

    /**
     * Remote method invocation
     *
     * @param conn         Connection to invoke method on
     * @param call         Service call context
     * @return             true on success
     */
    public boolean serviceCall(IConnection conn, IServiceCall call) {
		final IContext context = conn.getScope().getContext();
		if (call.getServiceName() != null) {
			context.getServiceInvoker().invoke(call, context);
		} else {
			context.getServiceInvoker().invoke(call, conn.getScope().getHandler());
		}
		return true;
	}

	/** {@inheritDoc} */
    public boolean start(IScope scope) {
		return true;
	}

	/** {@inheritDoc} */
    public void stop(IScope scope) {
		// do nothing here
	}

	/** {@inheritDoc} */
    public boolean handleEvent(IEvent event) {
		return false;
	}
}
