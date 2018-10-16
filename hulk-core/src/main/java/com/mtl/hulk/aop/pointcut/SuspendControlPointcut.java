package com.mtl.hulk.aop.pointcut;

import com.mtl.hulk.annotation.MTLSuspendControl;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

public class SuspendControlPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.getAnnotation(MTLSuspendControl.class) != null) {
            return true;
        }
        return false;
    }

}
