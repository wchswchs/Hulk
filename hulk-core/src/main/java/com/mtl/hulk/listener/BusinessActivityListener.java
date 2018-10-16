package com.mtl.hulk.listener;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mtl.hulk.HulkListener;
import com.mtl.hulk.HulkMvccExecutor;
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
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class BusinessActivityListener extends HulkListener {

    private final Logger logger = LoggerFactory.getLogger(BusinessActivityListener.class);

    private static final Map<String, CopyOnWriteArrayList<Future>> runFutures = new ConcurrentHashMap<String, CopyOnWriteArrayList<Future>>();
    private AtomicReference<HulkMvccExecutor> executor = new AtomicReference<HulkMvccExecutor>();
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
        if (runFutures.get(context.getActivity().getId().getSequence()) == null) {
            runFutures.put(context.getActivity().getId().getSequence(), new CopyOnWriteArrayList<Future>());
        }
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
                runFutures.get(context.getActivity().getId().getSequence()).add(runFuture);
            }
            for (Future rf : runFutures.get(context.getActivity().getId().getSequence())) {
                Object runResponse = rf.get();
                if (runResponse == null) {
                    return false;
                }
            }
        } finally {
            runFutures.get(context.getActivity().getId().getSequence()).clear();
            getExecutor().clear();
        }
        return true;
    }

    public HulkMvccExecutor getExecutor() {
        return executor.get();
    }

    public void setExecutor(HulkMvccExecutor executor) {
        this.executor.set(executor);
    }

    @Override
    public void destroy() {
        String transactionId = RuntimeContextHolder.getContext().getActivity().getId().getSequence();
        if (runFutures.size() > 0) {
            for (Future runFuture : runFutures.get(transactionId)) {
                FutureUtil.gracefulCancel(runFuture);
            }
            runFutures.get(transactionId).clear();
        }
        runExecutor.shutdown();
    }

    @Override
    public void destroyNow() {
        String transactionId = RuntimeContextHolder.getContext().getActivity().getId().getSequence();
        if (runFutures.size() > 0) {
            for (Future runFuture : runFutures.get(transactionId)) {
                FutureUtil.cancelNow(runFuture);
            }
            runFutures.get(transactionId).clear();
        }
        runExecutor.shutdownNow();
    }

    @Override
    public void closeFuture() {
        String transactionId = RuntimeContextHolder.getContext().getActivity().getId().getSequence();
        if (runFutures.size() > 0) {
            for (Future runFuture : runFutures.get(transactionId)) {
                FutureUtil.cancelNow(runFuture);
            }
            runFutures.get(transactionId).clear();
        }
    }

}
