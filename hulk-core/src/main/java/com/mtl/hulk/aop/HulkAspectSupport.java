package com.mtl.hulk.aop;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;

public abstract class HulkAspectSupport extends AbstractHulk implements BeanPostProcessor {

    protected BusinessActivityManagerImpl bam;

    public HulkAspectSupport(BusinessActivityManagerImpl bam, ApplicationContext apc) {
        super(apc);
        this.bam = bam;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        FeignClient annotation = null;
        if (bean.getClass().getInterfaces().length > 0) {
            annotation = bean.getClass().getInterfaces()[0].getAnnotation(FeignClient.class);
        }
        if (annotation != null) {
            bam.getClients().put(annotation.value(), bean);
        }
        return bean;
    }

}
