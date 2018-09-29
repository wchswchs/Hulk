package com.mtl.hulk;

import com.mtl.hulk.configuration.HulkProperties;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractHulk implements Resource {

    protected HulkProperties properties;
    protected ApplicationContext applicationContext;
    private Map<String, Object> clients = new ConcurrentHashMap<String, Object>();

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

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setProperties(HulkProperties properties) {
        this.properties = properties;
    }

    public Map<String, Object> getClients() {
        return clients;
    }

}
