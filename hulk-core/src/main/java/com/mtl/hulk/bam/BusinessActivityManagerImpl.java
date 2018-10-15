package com.mtl.hulk.bam;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.AbstractHulk;
import com.mtl.hulk.aop.interceptor.BrokerInterceptor;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.exception.ActionException;
import com.mtl.hulk.listener.BusinessActivityListener;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.BusinessActivityStatus;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.tools.ExecutorUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.text.MessageFormat;
import java.util.concurrent.*;

@SuppressWarnings("all")
public class BusinessActivityManagerImpl extends AbstractHulk implements BusinessActivityManager, UserBusinessActivity {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityManagerImpl.class);

    private final BusinessActivityListener listener;
    private final ExecutorService logExecutor = new ThreadPoolExecutor(properties.getLogThreadPoolSize(),
                                        Integer.MAX_VALUE, 5L,
                                        TimeUnit.SECONDS, new SynchronousQueue<>(),
                                        (new ThreadFactoryBuilder()).setNameFormat("Hulk-Log-Thread-%d").build());

    public BusinessActivityManagerImpl(HulkProperties properties, ApplicationContext applicationContext) {
        super(properties, applicationContext);
        this.listener = new BusinessActivityListener(properties, applicationContext);
    }

    /**
     * 发起事务并获取Try预留资源
     * @param methodInvocation
     * @return 获取执行Try的状态
     */
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
                    updateContext(tryResponse);
                }
            }
        } catch (Throwable ex) {
            logger.error("Try Request Exception", ex);
            return false;
        } finally {
            BrokerInterceptor.getTryFutures().clear();
        }
        return true;
    }

    /**
     * 发起事务提交
     * @return 提交结果
     * @throws Exception
     */
    @Override
    public boolean commit() throws Exception {
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTING);
        try {
            return listener.process();
        } catch (ActionException ex) {
            RuntimeContextHolder.getContext().setException(new com.mtl.hulk.HulkException(HulkErrorCode.COMMIT_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.COMMIT_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), ex.getAction())));
            throw ex;
        } catch (CancellationException ex) {
            throw ex;
        }
    }

    /**
     * 发起事务回滚
     * @return 回滚结果
     * @throws Exception
     */
    @Override
    public boolean rollback() throws Exception {
        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING);
        try {
            return listener.process();
        } catch (ActionException ex) {
            RuntimeContextHolder.getContext().setException(new com.mtl.hulk.HulkException(HulkErrorCode.ROLLBACK_FAIL.getCode(),
                    MessageFormat.format(HulkErrorCode.ROLLBACK_FAIL.getMessage(),
                            RuntimeContextHolder.getContext().getActivity().getId().formatString(), ex.getAction())));
            throw ex;
        } catch (CancellationException ex) {
            throw ex;
        }
    }

    /**
     * 合并Try预留资源
     * @param subContext
     */
    private void updateContext(Object subContext) {
        RuntimeContextHolder.getContext().getActivity().getAtomicTryActions().addAll(
                ((HulkContext) subContext).getRc().getActivity().getAtomicTryActions());
        RuntimeContextHolder.getContext().getActivity().getAtomicCommitActions().addAll(
                ((HulkContext) subContext).getRc().getActivity().getAtomicCommitActions());
        RuntimeContextHolder.getContext().getActivity().getAtomicRollbackActions().addAll(
                ((HulkContext) subContext).getRc().getActivity().getAtomicRollbackActions());
        BusinessActivityContextHolder.getContext().getParams().putAll(
                ((HulkContext) subContext).getBac().getParams());
    }

    public BusinessActivityListener getListener() {
        return listener;
    }

    public ExecutorService getLogExecutor() {
        return logExecutor;
    }

    @Override
    public void destroy() {
        listener.destroy();
        ExecutorUtil.gracefulShutdown(logExecutor);
    }

    @Override
    public void destroyNow() {
        listener.destroyNow();
        ExecutorUtil.shutdownNow(logExecutor);
    }

    @Override
    public void closeFuture() {
    }

}
