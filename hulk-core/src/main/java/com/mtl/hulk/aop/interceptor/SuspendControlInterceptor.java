package com.mtl.hulk.aop.interceptor;

import com.mtl.hulk.HulkInterceptor;
import com.mtl.hulk.annotation.MTLSuspendControl;
import com.mtl.hulk.common.Constants;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.model.BusinessActivityExecutionType;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class SuspendControlInterceptor implements HulkInterceptor, MethodInterceptor {

    private Map<String, CopyOnWriteArrayList<Thread>> commitTheads = new ConcurrentHashMap<String, CopyOnWriteArrayList<Thread>>();
    private AtomicReference<String> methodKey = new AtomicReference<String>();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String transactionId = "";
        if (RuntimeContextHolder.getContext().getActivity().getId() != null) {
            transactionId = RuntimeContextHolder.getContext().getActivity().getId().getSequence();
        } else {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            transactionId = request.getHeader(Constants.TRANSACTION_ID_HEADER_NAME);
        }

        Thread currentThread = Thread.currentThread();
        if (methodInvocation.getMethod().getAnnotation(MTLSuspendControl.class).value() ==
            BusinessActivityExecutionType.COMMIT) {
            methodKey.set(transactionId + "_" + methodInvocation.getMethod().getName());
            if (commitTheads.get(methodKey.get()) == null) {
                commitTheads.put(methodKey.get(), new CopyOnWriteArrayList<Thread>());
            }
            commitTheads.get(methodKey.get()).add(currentThread);
        }
        if (methodInvocation.getMethod().getAnnotation(MTLSuspendControl.class).value() ==
            BusinessActivityExecutionType.ROLLBACK) {
            if (commitTheads.size() > 0) {
                if (commitTheads.get(methodKey.get()).size() > 0) {
                    for (Thread t : commitTheads.get(methodKey.get())) {
                        t.interrupt();
                    }
                }
            }
        }

        Object ret = methodInvocation.proceed();
        if (ret != null && methodInvocation.getMethod().getAnnotation(MTLSuspendControl.class).value() ==
                BusinessActivityExecutionType.COMMIT) {
            commitTheads.get(methodKey.get()).remove(currentThread);
        }
        return (boolean) ret;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void destroyNow() {
    }

    @Override
    public void closeFuture() {
    }

}
