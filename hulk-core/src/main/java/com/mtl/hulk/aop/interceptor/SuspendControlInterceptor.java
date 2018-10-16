package com.mtl.hulk.aop.interceptor;

import com.mtl.hulk.HulkInterceptor;
import com.mtl.hulk.annotation.MTLSuspendControl;
import com.mtl.hulk.model.BusinessActivityExecutionType;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SuspendControlInterceptor implements HulkInterceptor, MethodInterceptor {

    private Map<Long, Thread> commitTheads = new ConcurrentHashMap<Long, Thread>();
    private Map<Long, Thread> rollbackTheads = new ConcurrentHashMap<Long, Thread>();
    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Thread currentThread = Thread.currentThread();
        Map<Long, Thread> threads = null;
        long requestId = INVOKE_ID.incrementAndGet();
        if (methodInvocation.getMethod().getAnnotation(MTLSuspendControl.class) != null) {
            if (methodInvocation.getMethod().getAnnotation(MTLSuspendControl.class).value() ==
                BusinessActivityExecutionType.COMMIT) {
                threads = commitTheads;
            }
            if (methodInvocation.getMethod().getAnnotation(MTLSuspendControl.class).value() ==
                BusinessActivityExecutionType.ROLLBACK) {
                threads = rollbackTheads;
                if (commitTheads.size() > 0) {
                    for (Thread t : commitTheads.values()) {
                        t.interrupt();
                    }
                }
            }
            threads.put(requestId, currentThread);
        }
        if (methodInvocation.proceed() != null) {
            threads.remove(requestId);
        }
        return true;
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
