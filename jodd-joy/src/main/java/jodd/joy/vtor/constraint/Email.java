// Copyright (c) 2003-present, Jodd Team (jodd.org). All Rights Reserved.

package jodd.joy.vtor.constraint;

import jodd.vtor.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Constraint(EmailConstraint.class)
public @interface Email {

	// ---------------------------------------------------------------- common

	/**
	 * Profiles.
	 */
	String[] profiles() default {};

	/**
	 * Severity.
	 */
	int severity() default 0;


}
