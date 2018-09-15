package com.mtl.hulk.aop;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.logger.BusinessActivityLoggerExceptionThread;
import com.mtl.hulk.logger.BusinessActivityLoggerThread;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;

public abstract class HulkAspectSupport extends AbstractHulk implements BeanFactoryAware, InitializingBean, SmartInitializingSingleton {

    private BeanFactory beanFactory;
    protected final BusinessActivityManagerImpl bam;

    public HulkAspectSupport(BusinessActivityManagerImpl bam) {
        super();
        this.bam = bam;
    }

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.loggerThread = new BusinessActivityLoggerThread(
                bam.getProperties(), bam.getListener().getDataSource());
        this.loggerExceptionThread = new BusinessActivityLoggerExceptionThread(bam.getProperties(), bam.getListener().getDataSource());
        bam.setLoggerThread(loggerThread);
        bam.setLoggerExceptionThread(loggerExceptionThread);
    }

}
