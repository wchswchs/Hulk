package com.mtl.hulk.bam;

import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.HulkDataSource;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.aop.interceptor.BrokerInterceptor;
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
import java.util.concurrent.*;

@SuppressWarnings("all")
public class BusinessActivityManagerImpl extends AbstractHulk implements BusinessActivityManager, UserBusinessActivity {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityManagerImpl.class);

    private BusinessActivityListener listener;

    public BusinessActivityManagerImpl(HulkProperties properties, HulkDataSource ds, ApplicationContext applicationContext) {
        super(properties, ds, applicationContext);
        this.listener = new BusinessActivityListener(ds);
    }

    @Override
    public boolean start(MethodInvocation methodInvocation) {
        listener.setBam(this);
        ExecutorService tryExecutor = getTryExecutor();
        setFuture(CompletableFuture.completedFuture(
                new ConcurrentHashMap<Integer, HulkContext>()));
        boolean status = false;
        try {
            BrokerInterceptor.setOrders(new CopyOnWriteArrayList<>());
            Object result = methodInvocation.proceed();
            if (RuntimeContextHolder.getContext().getActivity().getId() != null) {
                Map<Integer, HulkContext> hc = getFuture().join();
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
        } finally {
            if (getFuture() != null) {
                if (getFuture().isCompletedExceptionally() ||
                        getFuture().isDone()) {
                    getFuture().cancel(false);
                }
            }
        }
        return status;
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
