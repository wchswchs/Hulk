package com.mtl.hulk.aop.pointcut;

import com.mtl.hulk.annotation.MTLDTActivityID;
import com.mtl.hulk.annotation.MTLDTransaction;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

public class TransactionPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.getAnnotation(MTLDTActivityID.class) != null ||
                method.getAnnotation(MTLDTransaction.class) != null) {
            return true;
        }
        return false;
    }

}
