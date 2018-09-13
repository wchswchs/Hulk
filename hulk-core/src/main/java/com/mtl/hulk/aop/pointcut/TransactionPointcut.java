package com.mtl.hulk.aop.pointcut;

import com.mtl.hulk.annotation.MTLDTActivity;
import com.mtl.hulk.annotation.MTLTwoPhaseAction;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

public class TransactionPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.getAnnotation(MTLDTActivity.class) != null ||
                method.getAnnotation(MTLTwoPhaseAction.class) != null) {
            return true;
        }
        return false;
    }

}
