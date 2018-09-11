package com.mtl.hulk.annotation;

import com.mtl.hulk.model.AtomicActionCallType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MTLDTransaction {

    String confirmMethod() default "";

    String cancelMethod() default "";

    long timeout() default 30;

    AtomicActionCallType callType() default AtomicActionCallType.RequestResponse;

}
