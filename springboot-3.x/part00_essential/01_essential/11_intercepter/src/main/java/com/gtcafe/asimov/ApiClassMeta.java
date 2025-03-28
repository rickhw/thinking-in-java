package com.gtcafe.asimov;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiClassMeta {
    String description() default ""; // 描述信息
    boolean tenantAware() default true;
    String kind();
    // boolean serviceAware() default false;
}