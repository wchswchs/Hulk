package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
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
import com.mtl.hulk.logger.BusinessActivityLoggerThread;
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
        ExecutorService executor = bam.getTransactionExecutor();
        Integer result = 1;
        ExecutorService loggerExecutor = bam.getLogExecutor();
        try {
            status = bam.start(methodInvocation);
            if (status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.TRIED);
                future = executor.submit(new BusinessActivityExecutor(bam, new HulkContext(BusinessActivityContextHolder.getContext(),
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
            } else {
                loggerExecutor.submit(new BusinessActivityLoggerThread(bam.getProperties(), bam.getDataSource(),
                                    new HulkContext(BusinessActivityContextHolder.getContext(), RuntimeContextHolder.getContext())));
            }

            response = HulkResponseFactory.getResponse(result);
        } catch (TimeoutException ex) {
            RuntimeContextHolder.getContext().setException(new HulkException(HulkErrorCode.COMMIT_TIMEOUT.getCode(),
                    HulkErrorCode.COMMIT_TIMEOUT.getMessage()));
            bam.getRunFuture().cancel(true);
            bam.getRunExecutor().shutdownNow();
            future.cancel(false);
            future = executor.submit(new BusinessActivityExecutor(bam, new HulkContext(BusinessActivityContextHolder.getContext(),
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
            if (context.getActivity().getId() != null) {
                if (future != null) {
                    future.cancel(false);
                }
            }
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
        confirmServiceOperation.setService(bam.getApplicationContext().getId().split(":")[0]);
        confirmServiceOperation.setType(ServiceOperationType.TCC);
        confirmAction.setServiceOperation(confirmServiceOperation);
        confirmAction.setCallType(transaction.callType());
        activity.getAtomicCommitActions().add(confirmAction);

        AtomicAction cancelAction = new AtomicAction();
        ServiceOperation cancelServiceOperation = new ServiceOperation();
        cancelServiceOperation.setName(transaction.cancelMethod());
        cancelServiceOperation.setService(bam.getApplicationContext().getId().split(":")[0]);
        cancelServiceOperation.setType(ServiceOperationType.TCC);
        cancelAction.setServiceOperation(cancelServiceOperation);
        cancelAction.setCallType(transaction.callType());
        activity.getAtomicRollbackActions().add(cancelAction);

        context.setActivity(activity);
        RuntimeContextHolder.setContext(context);

        return true;
    }

}