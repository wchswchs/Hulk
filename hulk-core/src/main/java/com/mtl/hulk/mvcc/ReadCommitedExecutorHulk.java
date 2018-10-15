package com.mtl.hulk.mvcc;

import com.mtl.hulk.HulkMvccExecutor;
import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.hulk.exception.ActionException;
import com.mtl.hulk.listener.AtomicActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ReadCommitedExecutorHulk extends HulkMvccExecutor {

    private final Logger logger = LoggerFactory.getLogger(ReadCommitedExecutorHulk.class);

    @Override
    public boolean run(AtomicActionListener listener) {
        Map<String, Boolean> snapshot = listener.getSnapshot();
        logger.info("Transaction Executor running: {}", listener.getAction().getServiceOperation().getName());
        initMethod(listener);
        try {
            Method method = object.get().getClass().getMethod(listener.getAction().getServiceOperation().getName(), BusinessActivityContext.class);
            synchronized (snapshot) {
                boolean ret = (boolean) method.invoke(object.get(), args.get());
                snapshot.put(lockKey.get(), ret);
                return ret;
            }
        } catch (InvocationTargetException ex) {
            throw new ActionException(listener.getAction().getServiceOperation().getName(), ex);
        } catch (Exception ex) {
            throw new ActionException(listener.getAction().getServiceOperation().getName(), ex);
        }
    }

}
