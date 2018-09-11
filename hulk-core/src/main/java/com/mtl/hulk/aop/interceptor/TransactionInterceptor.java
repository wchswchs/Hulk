package com.mtl.hulk.aop.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.BusinessActivityIdSequenceFactory;
import com.mtl.hulk.HulkResponse;
import com.mtl.hulk.annotation.MTLDTActivityID;
import com.mtl.hulk.annotation.MTLDTransaction;
import com.mtl.hulk.aop.HulkAspectSupport;
import com.mtl.hulk.bam.BusinessActivityManagerImpl;
import com.mtl.hulk.context.*;
import com.mtl.hulk.model.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
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

        String response = "";
        boolean status = true;
        try {
            if (context.getActivity().getId() != null) {
                ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(50,
                        (new ThreadFactoryBuilder()).setNameFormat("TimeoutDetector-%d")
                                .setDaemon(true).build());
                ScheduledFuture timeoutFuture = scheduledExecutorService.schedule(
                        bam.getTimeoutDetector(),
                        RuntimeContextHolder.getContext().getActivity().getTimeout(), TimeUnit.SECONDS);

                if (timeoutFuture.isDone()) {
                    if ((Boolean) timeoutFuture.get()) {
                        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITING_FAILED);
                        return JSONObject.toJSONString(
                                new HulkResponse(1,
                                        RuntimeContextHolder.getContext().getActivity().getStatus().getDesc()));
                    }
                }
            }
            status = bam.start(context.getActivity().getAtomicTryActions().get(0), methodInvocation);
            if (status) {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.TRIED);
                status = bam.commit();
                if (!status) {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITING_FAILED);
                    status = bam.rollback();
                    if (!status) {
                        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKING_FAILED);
                    } else {
                        RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.ROLLBACKED);
                    }
                } else {
                    RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.COMMITTED);
                }
            } else {
                RuntimeContextHolder.getContext().getActivity().setStatus(BusinessActivityStatus.TRYING_EXPT);
            }

            if (context.getActivity().getId() == null) {
                HulkContext hulkContext = new HulkContext();
                hulkContext.setBac(BusinessActivityContextHolder.getContext());
                hulkContext.setRc(RuntimeContextHolder.getContext());
                return JSONObject.toJSONString(hulkContext);
            } else {
                loggerExecutor.submit(loggerThread);
            }

            if (!status) {
                if (RuntimeContextHolder.getContext().getActivity().getStatus() == BusinessActivityStatus.COMMITING_FAILED) {
                    response = JSONObject.toJSONString(
                            new HulkResponse(1,
                                    RuntimeContextHolder.getContext().getActivity().getStatus().getDesc()));
                } else {
                    response = JSONObject.toJSONString(
                            new HulkResponse(2,
                                    RuntimeContextHolder.getContext().getActivity().getStatus().getDesc()));
                }
            } else {
                response = JSONObject.toJSONString(new HulkResponse(0,
                        RuntimeContextHolder.getContext().getActivity().getStatus().getDesc()));
            }
        } catch (Throwable ex) {
            logger.error("Transaction Interceptor Error", ex);
        } finally {
            BusinessActivityContextHolder.clearContext();
            RuntimeContextHolder.clearContext();
        }

        return response;
    }

    private boolean prepareContext(MethodInvocation methodInvocation) {
        MTLDTActivityID activityID = methodInvocation.getMethod().getAnnotation(MTLDTActivityID.class);
        MTLDTransaction transaction = methodInvocation.getMethod().getAnnotation(MTLDTransaction.class);

        if (transaction == null) {
            return false;
        }

        BusinessActivityContext bac = BusinessActivityContextHolder.getContext();
        RuntimeContext context = RuntimeContextHolder.getContext();
        BusinessActivity activity = new BusinessActivity();

        bac.getParams().put(methodInvocation.getMethod().getName(), methodInvocation.getArguments());
        BusinessActivityContextHolder.setContext(bac);

        if (activityID != null) {
            BusinessActivityId id = new BusinessActivityId();
            id.setBusinessActivity(activityID.businessActivity());
            id.setBusinessDomain(activityID.businessDomain());
            id.setEntityId(activityID.entityId());
            id.setSequence(String.valueOf(BusinessActivityIdSequenceFactory.getSequence(bam.getProperties().getTransIdSequence()).nextId()));
            activity.setId(id);
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

        activity.setTimeout(transaction.timeout());

        context.setActivity(activity);
        RuntimeContextHolder.setContext(context);

        return true;
    }

}