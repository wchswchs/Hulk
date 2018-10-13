package com.mtl.hulk;

import com.mtl.hulk.common.Resource;
import com.mtl.hulk.configuration.HulkProperties;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractHulk implements Resource {

    protected volatile HulkProperties properties;
    protected volatile AtomicReference<ApplicationContext> applicationContext = new AtomicReference<ApplicationContext>();

    public AbstractHulk(HulkProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext.set(applicationContext);
    }

    public AbstractHulk(HulkProperties properties) {
        this.properties = properties;
    }

    public AbstractHulk(ApplicationContext applicationContext) {
        this.applicationContext.set(applicationContext);
    }

    public AbstractHulk() {
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext.set(applicationContext);
    }

    public void setProperties(HulkProperties properties) {
        this.properties = properties;
    }

    public HulkProperties getProperties() {
        return properties;
    }

}
