package com.GASB.slack_func.annotation;

import com.GASB.slack_func.validator.SpaceIdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SpaceIdValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSpaceId {
    String message() default "Invalid Space ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
