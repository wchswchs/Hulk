package com.mtl.hulk.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MTLSuspendControl {

    @AliasFor("type")
    String value() default "";

    @AliasFor("value")
    String type() default "";

}
