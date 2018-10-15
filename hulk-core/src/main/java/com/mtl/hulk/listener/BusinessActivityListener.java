package com.mtl.hulk.listener;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.HulkListener;
import com.mtl.hulk.configuration.HulkProperties;
import com.mtl.hulk.context.BusinessActivityContextHolder;
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

    private static final List<Future> runFutures = new CopyOnWriteArrayList<Future>();
    private final ExecutorService runExecutor = new ThreadPoolExecutor(properties.getActionthreadPoolSize(),
                                                Integer.MAX_VALUE, 10L,
                                                TimeUnit.SECONDS, new SynchronousQueue<>(),
                                                (new ThreadFactoryBuilder()).setNameFormat("Run-Thread-%d").build());

    public BusinessActivityListener(HulkProperties properties, ApplicationContext apc) {
        super(properties, apc);
    }

    /**
     * 执行一个事务方法集
     * @return
     * @throws Exception
     */
    @Override
    public boolean process() throws Exception {
        List<AtomicAction> currentActions = new CopyOnWriteArrayList<AtomicAction>();
        RuntimeContext context = RuntimeContextHolder.getContext();
        if (context.getActivity().getStatus() == BusinessActivityStatus.COMMITTING) {
            currentActions = context.getActivity().getAtomicCommitActions();
        }
        if (context.getActivity().getStatus() == BusinessActivityStatus.ROLLBACKING) {
            currentActions = context.getActivity().getAtomicRollbackActions();
        }
        try {
            for (int i = 0; i < context.getActivity().getAtomicTryActions().size(); i ++) {
                AtomicActionListener listener = new AtomicActionListener(currentActions.get(i), applicationContext,
                        context.getActivity().getAtomicTryActions().get(i), BusinessActivityContextHolder.getContext(),
                        context);
                String actionKey = "Transaction_" + context.getActivity().getId().getSequence()
                        + "_" + currentActions.get(i).getServiceOperation().getName();
                listener.getSnapshot().put(actionKey, false);
                listener.setProperties(properties);
                listener.setApplicationContext(applicationContext);
                Future<Boolean> runFuture = runExecutor.submit(new Callable<Boolean>() {
                        /**
                         * 异步执行事务方法
                         * @return
                         * @throws Exception
                         */
                        @Override
                        public Boolean call() throws Exception {
                            try {
                                return listener.process();
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                });
                runFutures.add(runFuture);
            }
            for (Future rf : runFutures) {
                Object runResponse = rf.get();
                if (runResponse == null) {
                    return false;
                }
            }
        } finally {
            runFutures.clear();
        }
        return true;
    }

    public static List<Future> getRunFutures() {
        return runFutures;
    }

    public ExecutorService getRunExecutor() {
        return runExecutor;
    }

    @Override
    public void destroy() {
        if (runFutures.size() > 0) {
            for (Future runFuture : runFutures) {
                FutureUtil.gracefulCancel(runFuture);
            }
            runFutures.clear();
        }
        runExecutor.shutdown();
    }

    @Override
    public void destroyNow() {
        if (runFutures.size() > 0) {
            for (Future runFuture : runFutures) {
                FutureUtil.cancelNow(runFuture);
            }
            runFutures.clear();
        }
        runExecutor.shutdownNow();
    }

    @Override
    public void closeFuture() {
        if (runFutures.size() > 0) {
            for (Future runFuture : runFutures) {
                FutureUtil.cancelNow(runFuture);
            }
            runFutures.clear();
        }
    }

}
