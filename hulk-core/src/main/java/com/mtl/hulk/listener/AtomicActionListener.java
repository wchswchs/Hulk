package com.mtl.hulk.listener;

import com.mtl.hulk.HulkListener;
import com.mtl.hulk.HulkMvccFactory;
import com.mtl.hulk.HulkMvccExecutor;
import com.mtl.hulk.context.*;
import com.mtl.hulk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class AtomicActionListener extends HulkListener {

    private final AtomicAction tryAction;
    private final BusinessActivityContext bac;
    private final RuntimeContext hc;
    private Map<String, Boolean> snapshot = new HashMap<String, Boolean>();
    private final Logger logger = LoggerFactory.getLogger(AtomicActionListener.class);

    public AtomicActionListener(AtomicAction action, ApplicationContext applicationContext, AtomicAction tryAction, BusinessActivityContext bac, RuntimeContext hc) {
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
            HulkMvccExecutor executor = HulkMvccFactory.getExecuter(hc.getActivity().getIsolationLevel());
            return executor.run(this);
        }
        return false;
    }

    public Map<String, Boolean> getSnapshot() {
        return snapshot;
    }

    public AtomicAction getTryAction() {
        return tryAction;
    }

    public BusinessActivityContext getBac() {
        return bac;
    }

    public RuntimeContext getHc() {
        return hc;
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
