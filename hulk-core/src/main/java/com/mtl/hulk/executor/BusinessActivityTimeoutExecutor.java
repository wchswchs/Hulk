package com.mtl.hulk.executor;

import com.mtl.hulk.HulkInterceptor;
import com.mtl.hulk.HulkResourceManager;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.context.RuntimeContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class BusinessActivityTimeoutExecutor implements Runnable {

    private final RuntimeContext rc;
    private final Future<Boolean> future;

    private static final Logger logger = LoggerFactory.getLogger(BusinessActivityTimeoutExecutor.class);

    public BusinessActivityTimeoutExecutor(Future<Boolean> future, RuntimeContext rc) {
        this.rc = rc;
        this.future = future;
    }

    @Override
    public void run() {
        RuntimeContextHolder.setContext(rc);
        if (!future.isDone()) {
            logger.error("Transaction Execute Timeout!");
            HulkResourceManager.getBam().getListener().closeFuture();
            for (HulkInterceptor interceptor : HulkResourceManager.getInterceptors()) {
                interceptor.closeFuture();
            }
        }
    }

}
