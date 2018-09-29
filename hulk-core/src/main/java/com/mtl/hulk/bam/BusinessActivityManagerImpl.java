package com.mtl.hulk.bam;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.aop.interceptor.BrokerInterceptor;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.listener.BusinessActivityListener;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.BusinessActivityStatus;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.util.ExecutorUtil;
import com.mtl.hulk.util.FutureUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

@SuppressWarnings("all")
public class BusinessActivityManagerImpl extends AbstractHulk implements BusinessActivityManager, UserBusinessActivity {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityManagerImpl.class);

    private final BusinessActivityListener listener;
    private CompletableFuture<Map<Integer, HulkContext>> tryFuture;
    private final ExecutorService logExecutor = new ThreadPoolExecutor(properties.getLogThreadPoolSize(),
                                        Integer.MAX_VALUE, 5L,
                                        TimeUnit.SECONDS, new SynchronousQueue<>(),
                                        (new ThreadFactoryBuilder()).setNameFormat("Hulk-Log-Thread-%d").build());

    public BusinessActivityManagerImpl(HulkProperties properties, ApplicationContext applicationContext) {
        super(properties, applicationContext);
        this.listener = new BusinessActivityListener(properties, applicationContext);
    }

    @Override
    public boolean start(MethodInvocation methodInvocation) {
        tryFuture = CompletableFuture.completedFuture(
                new ConcurrentHashMap<Integer, HulkContext>());
        boolean status = false;
        try {
            BrokerInterceptor.setOrders(new CopyOnWriteArrayList<>());
            Object result = methodInvocation.proceed();
            if (RuntimeContextHolder.getContext().getActivity().getId() != null) {
                Map<Integer, HulkContext> hc = tryFuture.join();
                if (hc != null && hc.size() > 0
                        && BrokerInterceptor.getOrders() != null
                        && BrokerInterceptor.getOrders().size() > 0) {
                    for (int i = 0; i < BrokerInterceptor.getOrders().size(); i++) {
                        if (hc.get(i) != null) {
                            RuntimeContextHolder.getContext().getActivity().getAtomicTryActions()
                                    .addAll(hc.get(i).getRc().getActivity().getAtomicTryActions());
                            RuntimeContextHolder.getContext().getActivity().getAtomicCommitActions()
                                    .addAll(hc.get(i).getRc().getActivity().getAtomicCommitActions());
                            RuntimeContextHolder.getContext().getActivity().getAtomicRollbackActions()
                                    .addAll(hc.get(i).getRc().getActivity().getAtomicRollbackActions());
                            BusinessActivityContextHolder.getContext().getParams().putAll(hc.get(i)
                                    .getBac().getParams());
                        }
                    }
                }
                status = true;
            }
        } catch (Throwable ex) {
            RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.TRY_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.TRY_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), methodInvocation.getMethod().getName())));
            logger.error("Hulk Try Exception", ex);
        }
        return status;
    }

    @Override
    public boolean commit() {
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTING);
        return listener.process();
    }

    @Override
    public boolean rollback() {
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING);
        return listener.process();
    }

    public BusinessActivityListener getListener() {
        return listener;
    }

    public HulkProperties getProperties() {
        return properties;
    }

    public CompletableFuture<Map<Integer, HulkContext>> getTryFuture() {
        return tryFuture;
    }

    public void setTryFuture(CompletableFuture<Map<Integer, HulkContext>> tryFuture) {
        this.tryFuture = tryFuture;
    }

    public ExecutorService getLogExecutor() {
        return logExecutor;
    }

    @Override
    public void destroy() {
        listener.destroy();
        FutureUtil.gracefulCancel(tryFuture);
        ExecutorUtil.gracefulShutdown(logExecutor);
        ExecutorUtil.shutdownNow(listener.getRunExecutor());
    }

    @Override
    public void destroyNow() {
        listener.destroyNow();
        ExecutorUtil.shutdownNow(listener.getRunExecutor());
    }

}
