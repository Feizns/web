package com.hhp.util.web.annotation;

import static com.hhp.util.web.annotation.RequestMethod.POST;
import static com.hhp.util.web.annotation.RequestMethod.GET;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface RequestMapping {
	
	/**
	 * for url pattern
	 * @return
	 */
	String value();
	
	String contentType() default "";
	
	RequestMethod[] method() default { GET, POST };
	
}
