package com.mtl.hulk;

import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.logger.BusinessActivityLoggerExceptionThread;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public abstract class AbstractHulk {

    protected HulkDataSource dataSource;
    protected HulkProperties properties;
    protected RuntimeContext context;
    protected BusinessActivityContext businessActivityContext;
    protected ApplicationContext applicationContext;
    protected ExecutorService transactionExecutor;
    protected ExecutorService tryExecutor;
    protected ExecutorService logExecutor;
    protected CompletableFuture<Map<Integer, HulkContext>> future;

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

    public AbstractHulk(HulkProperties properties, HulkDataSource ds, ApplicationContext applicationContext) {
        this.dataSource = ds;
        this.properties = properties;
        this.applicationContext = applicationContext;
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

    public void setContext(RuntimeContext context) {
        this.context = context;
    }

    public void setFuture(CompletableFuture<Map<Integer, HulkContext>> future) {
        this.future = future;
    }

    public CompletableFuture<Map<Integer, HulkContext>> getFuture() {
        return future;
    }

    public void setTransactionExecutor(ExecutorService transactionExecutor) {
        this.transactionExecutor = transactionExecutor;
    }

    public ExecutorService getTransactionExecutor() {
        return transactionExecutor;
    }

    public void setLogExecutor(ExecutorService logExecutor) {
        this.logExecutor = logExecutor;
    }

    public ExecutorService getLogExecutor() {
        return logExecutor;
    }

    public void setTryExecutor(ExecutorService tryExecutor) {
        this.tryExecutor = tryExecutor;
    }

    public ExecutorService getTryExecutor() {
        return tryExecutor;
    }

}
