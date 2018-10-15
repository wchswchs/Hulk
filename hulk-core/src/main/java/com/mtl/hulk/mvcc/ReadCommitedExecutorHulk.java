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

public class ReadCommitedExecutorHulk extends HulkMvccExecutor {

    private final Logger logger = LoggerFactory.getLogger(ReadCommitedExecutorHulk.class);

    @Override
    public boolean run(AtomicActionListener listener) {
        logger.info("Transaction Executor running: {}", listener.getAction().getServiceOperation().getName());
        initMethod(listener);
        long currentVersion = IncrTimeSequence.getInstance().nextId();
        snapshots.get(lockKey.get()).add(currentVersion);
        versionMap.put(currentVersion, args.get());
        if (currentVersion > snapshots.get(lockKey.get()).get(0)) {
            currentVersion = snapshots.get(lockKey.get()).get(snapshots.get(lockKey.get()).size() - 1);
        }
        try {
            Object args = versionMap.get(currentVersion);
            Method method = object.get().getClass().getMethod(listener.getAction().getServiceOperation().getName(), BusinessActivityContext.class);
            boolean ret = (boolean) method.invoke(object.get(), args);
            if (ret) {
                snapshots.get(lockKey.get()).remove(currentVersion);
            }
            return ret;
        } catch (InvocationTargetException ex) {
            throw new ActionException(listener.getAction().getServiceOperation().getName(), ex);
        } catch (Exception ex) {
            throw new ActionException(listener.getAction().getServiceOperation().getName(), ex);
        }
    }

}
