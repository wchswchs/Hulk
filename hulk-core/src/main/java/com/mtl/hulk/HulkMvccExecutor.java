package com.mtl.hulk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public abstract class HulkMvccExecutor {

    protected AtomicReference<String> actionKey = new AtomicReference<String>();
    protected Map<String, CopyOnWriteArrayList<Long>> snapshots = new HashMap<String, CopyOnWriteArrayList<Long>>();
    protected Map<Long, Object> undoMap = new HashMap<Long, Object>();

    public Object getActionArguments(long currentVersion) {
        return undoMap.get(currentVersion);
    }

    public void clear() {
        undoMap.clear();
        snapshots.clear();
    }

    public abstract long init(String actionKey, Object args);
    public abstract long getCurrentVersion(long version);

}
