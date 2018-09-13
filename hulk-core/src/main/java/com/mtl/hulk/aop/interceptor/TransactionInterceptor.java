package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.BusinessActivityIdSequenceFactory;
import com.mtl.hulk.HulkException;
import com.mtl.hulk.HulkResponse;
import com.mtl.hulk.HulkResponseFactory;
import com.mtl.hulk.annotation.MTLDTActivity;
import com.mtl.hulk.annotation.MTLTwoPhaseAction;
import com.mtl.hulk.aop.HulkAspectSupport;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.*;
import com.mtl.hulk.executor.BusinessActivityExecutor;
import com.mtl.hulk.message.HulkErrorCode;
import com.mtl.hulk.model.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.*;

public class TransactionInterceptor extends HulkAspectSupport implements MethodInterceptor, Serializable {

    private final static Logger logger = LoggerFactory.getLogger(TransactionInterceptor.class);

    public TransactionInterceptor(BusinessActivityManagerImpl bam) {
        super(bam);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!prepareContext(methodInvocation)) {
            return null;
        }

        RuntimeContext context = RuntimeContextHolder.getContext();

        HulkResponse response = null;
        boolean status = true;
        Future<Integer> future = null;
        ThreadPoolExecutor executor = null;
        Integer result = 1;
        try {
            status = bam.start(methodInvocation);
            if (status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.TRIED);
                executor = new ThreadPoolExecutor(50,
                        bam.getProperties().getLogThreadPoolSize(), 5L,
                        TimeUnit.SECONDS, new SynchronousQueue<>(),
                        (new ThreadFactoryBuilder()).setNameFormat("Transaction-Thread-%d").build());
                future = executor.submit(new BusinessActivityExecutor(bam));
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
            } else {
                loggerExecutor.submit(loggerThread);
            }

            response = HulkResponseFactory.getResponse(result);
        } catch (TimeoutException ex) {
            RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITING_FAILED);
            RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.COMMIT_TIMEOUT.getCode(),
                                                        HulkErrorCode.COMMIT_TIMEOUT.getMessage()));
            future = executor.submit(new BusinessActivityExecutor(bam));
            result = future.get(RuntimeContextHolder.getContext().getActivity().getTimeout(), TimeUnit.SECONDS);
            response = HulkResponseFactory.getResponse(result);
        } catch (Throwable ex) {
            logger.error("Transaction Interceptor Error", ex);
        } finally {
            BusinessActivityContextHolder.clearContext();
            RuntimeContextHolder.clearContext();
        }

        return JSONObject.toJSONString(response);
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
            id.setSequence(String.valueOf(BusinessActivityIdSequenceFactory.getSequence(bam.getProperties().getTransIdSequence()).nextId()));
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
        confirmServiceOperation.setType(ServiceOperationType.TCC);
        confirmAction.setServiceOperation(confirmServiceOperation);
        confirmAction.setCallType(transaction.callType());
        activity.getAtomicCommitActions().add(confirmAction);

        AtomicAction cancelAction = new AtomicAction();
        ServiceOperation cancelServiceOperation = new ServiceOperation();
        cancelServiceOperation.setName(transaction.cancelMethod());
        cancelServiceOperation.setType(ServiceOperationType.TCC);
        cancelAction.setServiceOperation(cancelServiceOperation);
        cancelAction.setCallType(transaction.callType());
        activity.getAtomicRollbackActions().add(cancelAction);

        context.setActivity(activity);
        RuntimeContextHolder.setContext(context);

        return true;
    }

}