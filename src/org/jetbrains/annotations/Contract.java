package org.jetbrains.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Source code recreated from a .class file by IntelliJ IDEA
 * (powered by Fernflower decompiler)
 *
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 4/21/16
 * <p>Time: 11:25 AM
 *
 * @author Miguel Mu\u00f1oz
 */

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface Contract {
	String value() default "";

	boolean pure() default false;
}
