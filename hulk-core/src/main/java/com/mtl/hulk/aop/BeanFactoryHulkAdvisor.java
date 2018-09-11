package com.mtl.hulk.aop;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

public class BeanFactoryHulkAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private final Pointcut pointcut;

    public BeanFactoryHulkAdvisor(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

}
