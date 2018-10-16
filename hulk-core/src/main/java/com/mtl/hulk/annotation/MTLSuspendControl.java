package com.mtl.hulk.annotation;

import com.mtl.hulk.model.BusinessActivityExecutionType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MTLSuspendControl {

    @AliasFor("type")
    BusinessActivityExecutionType value() default BusinessActivityExecutionType.COMMIT;

    @AliasFor("value")
    BusinessActivityExecutionType type() default BusinessActivityExecutionType.COMMIT;

}
