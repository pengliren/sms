package com.sms.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for public methods that should be protected by a named permission
 * when called through RTMP, RTMPT or Remoting.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeclareProtected {

	/**
	 * Permission required to execute method.
	 */
	String permission();
	
}
