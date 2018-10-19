package com.mtl.hulk.mvcc;

import com.mtl.hulk.HulkMvccExecutor;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class ReadCommitedExecutor extends HulkMvccExecutor {

    private static final AtomicLong TRANSACTION_METHOD_ID = new AtomicLong(0);

    @Override
    public long init(String actionKey, Object args) {
        this.actionKey.set(actionKey);
        long currentVersion = TRANSACTION_METHOD_ID.incrementAndGet();
        if (snapshots.get(actionKey) == null) {
            snapshots.put(actionKey, new CopyOnWriteArrayList<Long>());
        }
        snapshots.get(actionKey).add(currentVersion);
        undoMap.put(currentVersion, args);
        return currentVersion;
    }

    @Override
    public long getCurrentVersion(long version) {
        if (version <= snapshots.get(actionKey.get()).get(0)) {
            return version;
        }
        int index = snapshots.get(actionKey.get()).indexOf(version);
        if (index == -1) {
            index = 0;
        }
        version = snapshots.get(actionKey.get()).get(snapshots.get(actionKey.get()).size() - (index + 1));
        return getCurrentVersion(version);
    }

}
