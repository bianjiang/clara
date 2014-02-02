package edu.uams.clara.core.springmvc.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionParam {

	/**
	 * The name of the request parameter to bind to.
	 */
	String value() default "";

	/**
	 * Whether the parameter is required.
	 * <p>Default is <code>false</code>, return <code>null</code>,
	 * if it's true, it leads to an exception thrown in case
	 * of the parameter missing in the request.
	 */
	boolean required() default false;


}
