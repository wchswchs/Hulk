package com.mtl.hulk;

import com.mtl.hulk.context.RuntimeContext;
import com.mtl.hulk.listener.AtomicActionListener;
import com.mtl.hulk.model.AtomicAction;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public abstract class HulkMvccExecutor {

    protected AtomicReference<Object> object = new AtomicReference<Object>();
    protected AtomicReference<Object> args = new AtomicReference<Object>();
    protected AtomicReference<String> lockKey = new AtomicReference<String>();
    protected Map<String, CopyOnWriteArrayList<Long>> snapshots = new HashMap<String, CopyOnWriteArrayList<Long>>();
    protected Map<Long, Object> versionMap = new HashMap<Long, Object>();

    public void initMethod(AtomicActionListener listener) {
        ApplicationContext apc = listener.getApplicationContext();
        args.set(listener.getBac());
        RuntimeContext hc = listener.getHc();
        AtomicAction tryAction = listener.getTryAction();
        AtomicAction action = listener.getAction();
        if (apc.getId().split(":")[0].equals(action.getServiceOperation().getService())) {
            object.set(apc.getBean(tryAction.getServiceOperation().getBeanClass()));
        } else {
            object.set(HulkResourceManager.getClients().get(action.getServiceOperation().getService()));
        }
        lockKey.set("Transaction_" + hc.getActivity().getId().getSequence()
                + "_" + action.getServiceOperation().getName());
    }

    public Map<String, CopyOnWriteArrayList<Long>> getSnapshots() {
        return snapshots;
    }

    public abstract boolean run(AtomicActionListener listener);

}
