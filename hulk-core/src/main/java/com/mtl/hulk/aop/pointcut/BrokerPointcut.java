package com.mtl.hulk.aop.pointcut;

import com.mtl.hulk.annotation.MTLDTBroker;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

public class BrokerPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.getAnnotation(MTLDTBroker.class) != null) {
            return true;
        }
        return false;
    }

}
