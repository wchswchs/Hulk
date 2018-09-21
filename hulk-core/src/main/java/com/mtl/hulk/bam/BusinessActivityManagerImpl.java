package com.mtl.hulk.bam;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.listener.BusinessActivityListener;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.AtomicAction;
import com.mtl.hulk.model.BusinessActivityStatus;
import com.mtl.hulk.context.RuntimeContextHolder;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public class BusinessActivityManagerImpl extends AbstractHulk implements BusinessActivityManager, UserBusinessActivity {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityManagerImpl.class);

    private BusinessActivityListener listener;

    public BusinessActivityManagerImpl(HulkProperties properties, BusinessActivityListener listener, ApplicationContext applicationContext) {
        super(properties, applicationContext);
        this.listener = listener;
    }

    @Override
    public boolean start(MethodInvocation methodInvocation) {
        listener.setBam(this);
        ThreadPoolExecutor tryExecutor = new ThreadPoolExecutor(800,
                getProperties().getLogThreadPoolSize(), 5L,
                TimeUnit.SECONDS, new SynchronousQueue<>(),
                (new ThreadFactoryBuilder()).setNameFormat("try-thread-%d").build());
        setFuture(CompletableFuture.supplyAsync(() -> {
            return new HulkContext(
                    BusinessActivityContextHolder.getContext(),
                    RuntimeContextHolder.getContext());
        }, tryExecutor));
        try {
            Object result = methodInvocation.proceed();
        } catch (Throwable ex) {
            RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.TRY_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.TRY_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), methodInvocation.getMethod().getName())));
            logger.error("Hulk Try Exception", ex);
            return false;
        }
        if (RuntimeContextHolder.getContext().getActivity().getId() != null) {
            getFuture().join();
            return true;
        }
        return false;
    }

    @Override
    public boolean commit() {
        if (listener.getBam() == null) {
            listener.setBam(this);
        }
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTING);
        return listener.process();
    }

    @Override
    public boolean rollback() {
        if (listener.getBam() == null) {
            listener.setBam(this);
        }
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING);
        return listener.process();
    }

    @Override
    public List<AtomicAction> enlistAction(AtomicAction action, List<AtomicAction> actions) {
        return null;
    }

    @Override
    public boolean delistAction() {
        return true;
    }

    public BusinessActivityListener getListener() {
        return listener;
    }

    public HulkProperties getProperties() {
        return properties;
    }

}
