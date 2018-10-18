package com.mtl.hulk.configuration;

import com.mtl.hulk.serializer.KryoSerializer;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("mtl.hulk")
public class HulkProperties {

    private String loggerStorage = "mysql";
    private List<PoolProperties> logMasters;
    private List<PoolProperties> logSlaves;
    private int recoverySize = 20;
    private int logThreadPoolSize = 100;
    private int logMaxThreadPoolSize = Integer.MAX_VALUE;
    private int transactionThreadPoolSize = 200;
    private int tryThreadPoolSize = 200;
    private int tryMaxThreadPoolSize = 800;
    private int actionThreadPoolSize = 200;
    private int actionMaxThreadPoolSize = 1000;
    private Class<?> logSerialize = KryoSerializer.class;
    private String transIdSequence = "timestamp";
    private int retryTranactionCount = 3;

    public void setRetryTranactionCount(int retryTranactionCount) {
        this.retryTranactionCount = retryTranactionCount;
    }

    public int getRetryTranactionCount() {
        return retryTranactionCount;
    }

    public void setLoggerStorage(String loggerStorage) {
        this.loggerStorage = loggerStorage;
    }

    public String getLoggerStorage() {
        return loggerStorage;
    }

    public void setRecoverySize(int recoverySize) {
        this.recoverySize = recoverySize;
    }

    public int getRecoverySize() {
        return recoverySize;
    }

    public void setLogMasters(List<PoolProperties> logMasters) {
        this.logMasters = logMasters;
    }

    public List<PoolProperties> getLogMasters() {
        return logMasters;
    }

    public void setLogSlaves(List<PoolProperties> logSlaves) {
        this.logSlaves = logSlaves;
    }

    public List<PoolProperties> getLogSlaves() {
        return logSlaves;
    }

    public void setLogThreadPoolSize(int logThreadPoolSize) {
        this.logThreadPoolSize = logThreadPoolSize;
    }

    public int getLogThreadPoolSize() {
        return logThreadPoolSize;
    }

    public void setLogSerialize(Class<?> logSerialize) {
        this.logSerialize = logSerialize;
    }

    public Class<?> getLogSerialize() {
        return logSerialize;
    }

    public String getTransIdSequence() {
        return transIdSequence;
    }

    public void setTransIdSequence(String transIdSequence) {
        this.transIdSequence = transIdSequence;
    }

    public void setTransactionThreadPoolSize(int transactionThreadPoolSize) {
        this.transactionThreadPoolSize = transactionThreadPoolSize;
    }

    public int getTransactionThreadPoolSize() {
        return transactionThreadPoolSize;
    }

    public void setTryThreadPoolSize(int tryhreadPoolSize) {
        this.tryThreadPoolSize = tryhreadPoolSize;
    }

    public int getTrythreadPoolSize() {
        return tryThreadPoolSize;
    }

    public void setActionthreadPoolSize(int actionThreadPoolSize) {
        this.actionThreadPoolSize = actionThreadPoolSize;
    }

    public int getActionthreadPoolSize() {
        return actionThreadPoolSize;
    }

    public void setActionMaxThreadPoolSize(int actionMaxThreadPoolSize) {
        this.actionMaxThreadPoolSize = actionMaxThreadPoolSize;
    }

    public int getActionMaxThreadPoolSize() {
        return actionMaxThreadPoolSize;
    }

    public void setLogMaxThreadPoolSize(int logMaxThreadPoolSize) {
        this.logMaxThreadPoolSize = logMaxThreadPoolSize;
    }

    public int getLogMaxThreadPoolSize() {
        return logMaxThreadPoolSize;
    }

    public void setTryMaxThreadPoolSize(int tryMaxThreadPoolSize) {
        this.tryMaxThreadPoolSize = tryMaxThreadPoolSize;
    }

    public int getTryMaxThreadPoolSize() {
        return tryMaxThreadPoolSize;
    }

}
