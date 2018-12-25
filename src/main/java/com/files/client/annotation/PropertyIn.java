package com.files.client.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyIn {

	Class<?> enumCls() default String.class;

	String matchKey() default "";

	String refProp() default "";

	String sortBy() default "";
	
	String dateFormat() default "";
}
