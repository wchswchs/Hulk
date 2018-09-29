package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.mtl.hulk.*;
import com.mtl.hulk.annotation.MTLDTActivity;
import com.mtl.hulk.annotation.MTLTwoPhaseAction;
import com.mtl.hulk.aop.HulkAspectSupport;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.*;
import com.mtl.hulk.executor.BusinessActivityExecutor;
import com.mtl.hulk.logger.BusinessActivityLoggerThread;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.*;
import com.mtl.hulk.util.ExecutorUtil;
import com.mtl.hulk.util.FutureUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.concurrent.*;

public class TransactionInterceptor extends HulkAspectSupport implements HulkInterceptor, MethodInterceptor, Serializable {

    private final static Logger logger = LoggerFactory.getLogger(TransactionInterceptor.class);

    private final ExecutorService transactionExecutor = Executors.newFixedThreadPool(properties.getTransactionThreadPoolSize());
    private Future<Integer> future;

    public TransactionInterceptor(HulkProperties properties, ApplicationContext apc) {
        super(properties, apc);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!prepareContext(methodInvocation)) {
            return null;
        }

        RuntimeContext context = RuntimeContextHolder.getContext();

        HulkResponse response = null;
        boolean status = true;
        Integer result = 1;
        ExecutorService loggerExecutor = HulkResourceManager.getBam().getLogExecutor();
        try {
            status = HulkResourceManager.getBam().start(methodInvocation);
            if (status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.TRIED);
                future = transactionExecutor.submit(new BusinessActivityExecutor(new HulkContext(BusinessActivityContextHolder.getContext(),
                                        RuntimeContextHolder.getContext())));
                result = future.get(RuntimeContextHolder.getContext().getActivity().getTimeout(), TimeUnit.SECONDS);
            } else {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.TRYING_EXPT);
                result = BooleanUtils.toInteger(status);
            }

            if (context.getActivity().getId() == null) {
                HulkContext hulkContext = new HulkContext();
                hulkContext.setBac(BusinessActivityContextHolder.getContext());
                hulkContext.setRc(RuntimeContextHolder.getContext());
                return JSONObject.toJSONString(hulkContext);
            }

            loggerExecutor.submit(new BusinessActivityLoggerThread(properties,
                                    new HulkContext(BusinessActivityContextHolder.getContext(), RuntimeContextHolder.getContext())));
            response = HulkResponseFactory.getResponse(result);
        } catch (TimeoutException ex) {
            RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.COMMIT_TIMEOUT.getCode(),
                    HulkErrorCode.COMMIT_TIMEOUT.getMessage()));
            destroyNow();
            future = transactionExecutor.submit(new BusinessActivityExecutor(new HulkContext(BusinessActivityContextHolder.getContext(),
                                    RuntimeContextHolder.getContext())));
            result = future.get(RuntimeContextHolder.getContext().getActivity().getTimeout(), TimeUnit.SECONDS);
            response = HulkResponseFactory.getResponse(result);
        } catch (NullPointerException ex) {
            logger.error("Transaction Interceptor Error", ex);
        } catch (Exception ex) {
            logger.error("Transaction Interceptor Error", ex);
        } finally {
            BusinessActivityContextHolder.clearContext();
            RuntimeContextHolder.clearContext();
        }
        return JSONObject.toJSONString(response);
    }

    @Override
    public void destroy() {
        FutureUtil.gracefulCancel(future);
        ExecutorUtil.gracefulShutdown(transactionExecutor);
    }

    @Override
    public void destroyNow() {
        HulkResourceManager.getBam().getListener().destroyNow();
        FutureUtil.cancelNow(future);
    }

    private boolean prepareContext(MethodInvocation methodInvocation) {
        MTLDTActivity activityAnnotation = methodInvocation.getMethod().getAnnotation(MTLDTActivity.class);
        MTLTwoPhaseAction transaction = methodInvocation.getMethod().getAnnotation(MTLTwoPhaseAction.class);

        if (transaction == null) {
            return false;
        }

        BusinessActivityContext bac = BusinessActivityContextHolder.getContext();
        RuntimeContext context = RuntimeContextHolder.getContext();
        BusinessActivity activity = new BusinessActivity();

        bac.getParams().put(methodInvocation.getMethod().getName(), methodInvocation.getArguments());
        BusinessActivityContextHolder.setContext(bac);

        if (activityAnnotation != null) {
            BusinessActivityId id = new BusinessActivityId();
            id.setBusinessActivity(activityAnnotation.businessActivity());
            id.setBusinessDomain(activityAnnotation.businessDomain());
            id.setEntityId(activityAnnotation.entityId());
            id.setSequence(String.valueOf(BusinessActivityIdSequenceFactory.getSequence(properties.getTransIdSequence()).nextId()));
            activity.setId(id);
            activity.setTimeout(activityAnnotation.timeout());
        }
        activity.setStatus(BusinessActivityStatus.TRYING);

        AtomicAction tryAction = new AtomicAction();
        ServiceOperation tryServiceOperation = new ServiceOperation();
        tryServiceOperation.setName(methodInvocation.getMethod().getName());
        tryServiceOperation.setBeanClass(StringUtils.uncapitalize(methodInvocation.getMethod().getDeclaringClass().getSimpleName()));
        tryServiceOperation.setType(ServiceOperationType.TCC);
        tryAction.setServiceOperation(tryServiceOperation);
        tryAction.setCallType(transaction.callType());
        activity.getAtomicTryActions().add(tryAction);

        AtomicAction confirmAction = new AtomicAction();
        ServiceOperation confirmServiceOperation = new ServiceOperation();
        confirmServiceOperation.setName(transaction.confirmMethod());
        confirmServiceOperation.setService(applicationContext.getId().split(":")[0]);
        confirmServiceOperation.setType(ServiceOperationType.TCC);
        confirmAction.setServiceOperation(confirmServiceOperation);
        confirmAction.setCallType(transaction.callType());
        activity.getAtomicCommitActions().add(confirmAction);

        AtomicAction cancelAction = new AtomicAction();
        ServiceOperation cancelServiceOperation = new ServiceOperation();
        cancelServiceOperation.setName(transaction.cancelMethod());
        cancelServiceOperation.setService(applicationContext.getId().split(":")[0]);
        cancelServiceOperation.setType(ServiceOperationType.TCC);
        cancelAction.setServiceOperation(cancelServiceOperation);
        cancelAction.setCallType(transaction.callType());
        activity.getAtomicRollbackActions().add(cancelAction);

        context.setActivity(activity);
        RuntimeContextHolder.setContext(context);

        return true;
    }

}