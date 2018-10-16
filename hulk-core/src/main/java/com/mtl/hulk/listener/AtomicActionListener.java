package com.mtl.hulk.listener;

import com.mtl.hulk.HulkListener;
import com.mtl.hulk.HulkMvccExecutor;
import com.mtl.hulk.HulkMvccFactory;
import com.mtl.hulk.HulkResourceManager;
import com.mtl.hulk.context.*;
import com.mtl.hulk.exception.ActionException;
import com.mtl.hulk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AtomicActionListener extends HulkListener {

    private volatile AtomicAction tryAction;
    private volatile BusinessActivityContext bac;
    private volatile RuntimeContext hc;
    private final Logger logger = LoggerFactory.getLogger(AtomicActionListener.class);

    public AtomicActionListener(AtomicAction action, ApplicationContext applicationContext, AtomicAction tryAction,
                                BusinessActivityContext bac, RuntimeContext hc) {
        super(action, applicationContext);
        this.tryAction = tryAction;
        this.bac = bac;
        this.hc = hc;
    }

    /**
     * 事务方法执行
     * @return
     * @throws Exception
     */
    @Override
    public boolean process() throws Exception {
        if (action.getServiceOperation().getType() == ServiceOperationType.TCC) {
            Object object = null;
            Object args = null;
            try {
                if (applicationContext.getId().split(":")[0].equals(action.getServiceOperation().getService())) {
                    object = applicationContext.getBean(tryAction.getServiceOperation().getBeanClass());
                } else {
                    object = HulkResourceManager.getClients().get(action.getServiceOperation().getService());
                }
                logger.info("Transaction Executor running: {}", action.getServiceOperation().getName());
                Method method = object.getClass().getMethod(action.getServiceOperation().getName(), BusinessActivityContext.class);
                String[] aid = hc.getActivity().getId().formatString().split("_");
                HulkMvccExecutor mvccExecutor = HulkMvccFactory.getExecuter(hc.getActivity().getIsolationLevel());
                if (mvccExecutor == null) {
                    args = bac;
                } else {
                    long currentVersion = mvccExecutor.init(
                            "Transaction_" + aid[0] + "_" + aid[1] + "_" + action.getServiceOperation().getName(), bac);
                    args = mvccExecutor.getActionArguments(mvccExecutor.getCurrentVersion(currentVersion));
                }
                Object ret = method.invoke(object, args);
                if (((boolean) ret) == false) {
                    return false;
                }
            } catch (InvocationTargetException ex) {
                throw new ActionException(action.getServiceOperation().getName(), ex);
            } catch (Exception ex) {
                throw new ActionException(action.getServiceOperation().getName(), ex);
            }
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
