package com.mtl.hulk.listener;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.HulkListener;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.BusinessActivityContextHolder;
import com.mtl.hulk.context.HulkContext;
import com.mtl.hulk.model.AtomicAction;
import com.mtl.hulk.model.BusinessActivityStatus;
import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.context.RuntimeContextHolder;
import com.mtl.hulk.tools.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.*;

public class BusinessActivityListener extends HulkListener {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityListener.class);

    private final ExecutorService runExecutor = new ThreadPoolExecutor(properties.getActionthreadPoolSize(),
                                                Integer.MAX_VALUE, 10L,
                                                TimeUnit.SECONDS, new SynchronousQueue<>(),
                                                (new ThreadFactoryBuilder()).setNameFormat("Run-Thread-%d").build());
    private volatile CompletableFuture<HulkContext> runFuture;

    public BusinessActivityListener(HulkProperties properties, ApplicationContext apc) {
        super(properties, apc);
    }

    @Override
    public boolean process() {
        List<AtomicAction> currentActions = new CopyOnWriteArrayList<AtomicAction>();
        RuntimeContext context = RuntimeContextHolder.getContext();
        if (context.getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
            currentActions = context.getActivity().getAtomicCommitActions();
        }
        if (context.getActivity().getStatus() == BusinessActivityStatus.ROLLBACKING) {
            currentActions = context.getActivity().getAtomicRollbackActions();
        }
        runFuture = CompletableFuture.completedFuture(
                                        new HulkContext(BusinessActivityContextHolder.getContext(),
                                        RuntimeContextHolder.getContext()));
        for (int i = 0; i < context.getActivity().getAtomicTryActions().size(); i ++) {
            AtomicActionListener listener = new AtomicActionListener(currentActions.get(i), applicationContext.get(),
                                            context.getActivity().getAtomicTryActions().get(i));
            listener.setProperties(properties);
            listener.setApplicationContext(applicationContext.get());
            boolean status = listener.process();
            if (status == false) {
                return false;
            }
        }
        HulkContext ret = runFuture.join();
        if (ret.getRc().getException() != null && ret.getRc().getException().getCode() > 0) {
            return false;
        }
        return true;
    }

    public ExecutorService getRunExecutor() {
        return runExecutor;
    }

    public CompletableFuture<HulkContext> getRunFuture() {
        return runFuture;
    }

    public void setRunFuture(CompletableFuture<HulkContext> runFuture) {
        this.runFuture = runFuture;
    }

    @Override
    public void destroy() {
        FutureUtil.gracefulCancel(runFuture);
    }

    @Override
    public void destroyNow() {
        FutureUtil.cancelNow(runFuture);
    }

}
