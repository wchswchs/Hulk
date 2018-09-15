package com.mtl.hulk;

import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.logger.BusinessActivityLoggerExceptionThread;
import com.mtl.hulk.logger.BusinessActivityLoggerThread;
import org.springframework.context.ApplicationContext;

public abstract class AbstractHulk {

    protected HulkDataSource dataSource;
    protected HulkProperties properties;
    protected RuntimeContext context;
    protected BusinessActivityContext businessActivityContext;
    protected ApplicationContext applicationContext;
    protected BusinessActivityLoggerThread loggerThread;
    protected BusinessActivityLoggerExceptionThread loggerExceptionThread;

    public AbstractHulk(HulkDataSource dataSource, HulkProperties properties, RuntimeContext context, ApplicationContext applicationContext) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.context = context;
        this.applicationContext = applicationContext;
    }

    public AbstractHulk(HulkDataSource dataSource, HulkProperties properties, RuntimeContext context) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.context = context;
    }

    public AbstractHulk(HulkProperties properties, HulkDataSource ds) {
        this.properties = properties;
        this.dataSource = ds;
    }

    public AbstractHulk(HulkProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    public AbstractHulk(HulkDataSource ds, ApplicationContext applicationContext) {
        this.dataSource = ds;
        this.applicationContext = applicationContext;
    }

    public AbstractHulk(HulkDataSource ds, HulkProperties properties) {
        this.dataSource = ds;
        this.properties = properties;
    }

    public AbstractHulk(HulkProperties properties) {
        this.properties = properties;
    }

    public AbstractHulk(HulkDataSource ds) {
        this.dataSource = ds;
    }

    public AbstractHulk(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AbstractHulk() {

    }

    public void setDataSource(HulkDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public HulkDataSource getDataSource() {
        return dataSource;
    }

    public void setLoggerThread(BusinessActivityLoggerThread loggerThread) {
        this.loggerThread = loggerThread;
    }

    public void setLoggerExceptionThread(BusinessActivityLoggerExceptionThread loggerExceptionThread) {
        this.loggerExceptionThread = loggerExceptionThread;
    }

    public void setContext(RuntimeContext context) {
        this.context = context;
    }

}
