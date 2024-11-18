package com.gtcafe.asimov.system.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateRequest {
    boolean validatePayload() default true;
    boolean validateQuota() default true;
    boolean validateAssociations() default true;
}