package com.mtl.hulk.mvcc;

import com.mtl.hulk.HulkMvccExecutor;
import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.exception.ActionException;
import com.mtl.hulk.listener.AtomicActionListener;
import com.mtl.hulk.sequence.IncrTimeSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReadCommitedExecutorHulk extends HulkMvccExecutor {

    private final Logger logger = LoggerFactory.getLogger(ReadCommitedExecutorHulk.class);

    @Override
    public boolean run(AtomicActionListener listener) {
        logger.info("Transaction Executor running: {}", listener.getAction().getServiceOperation().getName());
        initMethod(listener);
        long currentVersion = IncrTimeSequence.getInstance().nextId();
        if (snapshots.get(actionKey.get()) == null) {
            snapshots.put(actionKey.get(), new CopyOnWriteArrayList<Long>());
        }
        snapshots.get(actionKey.get()).add(currentVersion);
        versionMap.put(currentVersion, args.get());
        if (currentVersion > snapshots.get(actionKey.get()).get(0)) {
            currentVersion = snapshots.get(actionKey.get()).get(snapshots.get(actionKey.get()).size() - 1);
        }
        try {
            Object methodArgs = versionMap.get(currentVersion);
            Method method = obj.get().getClass().getMethod(listener.getAction().getServiceOperation().getName(), BusinessActivityContext.class);
            boolean ret = (boolean) method.invoke(obj.get(), methodArgs);
            if (ret) {
                snapshots.get(actionKey.get()).remove(currentVersion);
            }
            return ret;
        } catch (InvocationTargetException ex) {
            throw new ActionException(listener.getAction().getServiceOperation().getName(), ex);
        } catch (Exception ex) {
            throw new ActionException(listener.getAction().getServiceOperation().getName(), ex);
        }
    }

}
