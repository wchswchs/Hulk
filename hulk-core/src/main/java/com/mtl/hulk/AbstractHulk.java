package com.mtl.hulk;

import com.mtl.hulk.common.Resource;
import com.mtl.hulk.configuration.HulkProperties;
import org.springframework.context.ApplicationContext;

public abstract class AbstractHulk implements Resource {

    protected HulkProperties properties;
    protected ApplicationContext applicationContext;

    public AbstractHulk(HulkProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    public AbstractHulk(HulkProperties properties) {
        this.properties = properties;
    }

    public AbstractHulk(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AbstractHulk() {
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setProperties(HulkProperties properties) {
        this.properties = properties;
    }

    public HulkProperties getProperties() {
        return properties;
    }

}
