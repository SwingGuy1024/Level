package com.neptunedreams.util;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/3/15
 * <p>Time: 10:20 AM
 *
 * @author Miguel Mu\u00f1oz
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface NotNull {
	String value() default "";
}

