package com.mtl.hulk.aop;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.HulkResourceManager;
import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.HulkInterceptor;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.configuration.HulkProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;

public abstract class HulkAspectSupport extends AbstractHulk implements BeanPostProcessor {

    public HulkAspectSupport(HulkProperties properties, ApplicationContext apc) {
        super(properties, apc);
    }

    public HulkAspectSupport(HulkProperties properties) {
        super(properties);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("bam")) {
            HulkResourceManager.setBam((BusinessActivityManagerImpl) bean);
        }
        if (beanName.equals("hulkDataSource")) {
            HulkResourceManager.setDatasource((HulkDataSource) bean);
        }
        if (beanName.equals("hulkTransactionInterceptor")
                    || beanName.equals("hulkBrokerInterceptor")) {
            HulkResourceManager.getInterceptors().add((HulkInterceptor) bean);
        }
        FeignClient annotation = null;
        if (bean.getClass().getInterfaces().length > 0) {
            annotation = bean.getClass().getInterfaces()[0].getAnnotation(FeignClient.class);
        }
        if (annotation != null) {
            HulkResourceManager.getClients().put(annotation.value(), bean);
        }
        return bean;
    }

}
