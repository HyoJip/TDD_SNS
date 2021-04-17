package com.hoaxify.hoxaxify.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.FIELD)
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {
	String message() default "{hoaxify.constraints.username.UniqueUsername.message}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
