package com.mtl.hulk.annotation;

import com.mtl.hulk.model.AtomicActionCallType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MTLTwoPhaseAction {

    String confirmMethod() default "";

    String cancelMethod() default "";

    AtomicActionCallType callType() default AtomicActionCallType.RequestResponse;

}
