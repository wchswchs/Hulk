package com.mtl.hulk.annotation;

import com.mtl.hulk.model.BusinessActivityIsolationLevel;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface MTLDTActivity {

    String businessDomain() default "";

    String businessActivity() default "";

    String entityId() default "";

    long timeout() default 5;

    BusinessActivityIsolationLevel isolationLevel() default BusinessActivityIsolationLevel.READ_UNCOMMITTED;

}
