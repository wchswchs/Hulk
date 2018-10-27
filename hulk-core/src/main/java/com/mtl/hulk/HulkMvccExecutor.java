package com.mtl.hulk;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public abstract class HulkMvccExecutor {

    protected AtomicReference<String> actionKey = new AtomicReference<String>();
    protected Map<String, CopyOnWriteArrayList<Long>> snapshots = new ConcurrentHashMap<String, CopyOnWriteArrayList<Long>>();
    protected Map<Long, Object> undoMap = new ConcurrentHashMap<Long, Object>();

    public Object getActionArguments(long currentVersion) {
        return undoMap.get(currentVersion);
    }

    public void removeAll(String actionKey, List<Long> versions) {
        for (Long version : versions) {
            undoMap.remove(version);
        }
        for (Long version : versions) {
            if (snapshots.get(actionKey).indexOf(version) >= 0) {
                snapshots.get(actionKey).remove(version);
            }
        }
    }

    public void clear() {
        undoMap.clear();
        snapshots.clear();
    }

    public abstract long init(String actionKey, Object args);
    public abstract List<Long> getCurrentVersion(long version);

}
