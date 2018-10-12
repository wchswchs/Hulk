package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.HulkInterceptor;
import com.mtl.hulk.aop.HulkAspectSupport;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.*;
import com.mtl.hulk.tools.ExecutorUtil;
import com.mtl.hulk.tools.FutureUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.*;

public class BrokerInterceptor extends HulkAspectSupport implements HulkInterceptor, MethodInterceptor, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BrokerInterceptor.class);

    private final ExecutorService tryExecutor = new ThreadPoolExecutor(properties.getTrythreadPoolSize(),
                            Integer.MAX_VALUE, 10L,
                            TimeUnit.SECONDS, new SynchronousQueue<>(),
                                    (new ThreadFactoryBuilder()).setNameFormat("Try-Thread-%d").build());
    private static final List<Future> tryFutures = new CopyOnWriteArrayList<Future>();

    public BrokerInterceptor(HulkProperties properties) {
        super(properties);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Future<HulkContext> tryFuture = tryExecutor.submit(new Callable<HulkContext>() {
            @Override
            public HulkContext call() throws Exception {
                logger.info("Try Request: {}", methodInvocation.getMethod().getName());
                try {
                    return JSONObject.parseObject((String)methodInvocation.proceed(), HulkContext.class);
                } catch (Throwable t) {
                    logger.error("Broker Interceptor Exception", t);
                }
                return null;
            }
        });
        tryFutures.add(tryFuture);
        return "ok";
    }

    public static List<Future> getTryFutures() {
        return tryFutures;
    }

    @Override
    public void destroy() {
        if (tryFutures.size() > 0) {
            for (Future tryFuture : tryFutures) {
                FutureUtil.gracefulCancel(tryFuture);
            }
            tryFutures.clear();
        }
        ExecutorUtil.gracefulShutdown(tryExecutor);
    }

    @Override
    public void destroyNow() {
        if (tryFutures.size() > 0) {
            for (Future tryFuture : tryFutures) {
                FutureUtil.cancelNow(tryFuture);
            }
            tryFutures.clear();
        }
        ExecutorUtil.shutdownNow(tryExecutor);
    }

    @Override
    public void closeFuture() {
        if (tryFutures.size() > 0) {
            for (Future tryFuture : tryFutures) {
                FutureUtil.cancelNow(tryFuture);
            }
            tryFutures.clear();
        }
    }

}