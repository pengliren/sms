package com.sms.server.api;

import com.sms.server.api.event.IEventDispatcher;
import com.sms.server.api.event.IEventHandler;
import com.sms.server.api.event.IEventListener;


public interface ICoreObject extends ICastingAttributeStore, IEventDispatcher, IEventHandler, IEventListener {

}
