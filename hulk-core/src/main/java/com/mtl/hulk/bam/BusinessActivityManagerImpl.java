package com.mtl.hulk.bam;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.aop.interceptor.BrokerInterceptor;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.exception.HulkException;
import com.mtl.hulk.listener.BusinessActivityListener;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.BusinessActivityStatus;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.tools.ExecutorUtil;
import com.mtl.hulk.tools.FutureUtil;
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
    private volatile CompletableFuture<Map<Integer, HulkContext>> tryFuture;
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
        try {
            Object result = methodInvocation.proceed();
            if (RuntimeContextHolder.getContext().getActivity().getId() != null) {
                for (Future tryFuture : BrokerInterceptor.getTryFutures()) {
                    Object tryResponse = tryFuture.get();
                    if (tryResponse == null) {
                        return false;
                    }
                    RuntimeContextHolder.getContext().getActivity().getAtomicTryActions().addAll(((HulkContext)tryResponse).getRc().getActivity().getAtomicTryActions());
                    RuntimeContextHolder.getContext().getActivity().getAtomicCommitActions().addAll(((HulkContext)tryResponse).getRc().getActivity().getAtomicCommitActions());
                    RuntimeContextHolder.getContext().getActivity().getAtomicRollbackActions().addAll(((HulkContext)tryResponse).getRc().getActivity().getAtomicRollbackActions());
                    BusinessActivityContextHolder.getContext().getParams().putAll(((HulkContext)tryResponse).getBac().getParams());
                }
            }
        } catch (Throwable ex) {
            RuntimeContextHolder.getContext().setException(new com.mtl.hulk.HulkException(HulkErrorCode.TRY_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.TRY_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), methodInvocation.getMethod().getName())));
            logger.error("Hulk Try Exception", ex);
            return false;
        } finally {
            BrokerInterceptor.getTryFutures().clear();
        }
        return true;
    }

    @Override
    public boolean commit() {
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTING);
        try {
            return listener.process();
        } catch (Exception ex) {
            RuntimeContextHolder.getContext().setException(new com.mtl.hulk.HulkException(HulkErrorCode.COMMIT_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.COMMIT_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), ((HulkException) ex).getAction())));
            logger.error("Hulk Commit Exception", ex);
        }
        return true;
    }

    @Override
    public boolean rollback() {
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING);
        try {
            return listener.process();
        } catch (Exception ex) {
            RuntimeContextHolder.getContext().setException(new com.mtl.hulk.HulkException(HulkErrorCode.ROLLBACK_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.ROLLBACK_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), ((HulkException) ex).getAction())));
            logger.error("Hulk Rollback Exception", ex);
        }
        return true;
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
