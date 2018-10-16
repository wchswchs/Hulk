package com.mtl.hulk.mvcc;

import com.mtl.hulk.HulkMvccExecutor;
import com.mtl.hulk.model.BusinessIdForSequence;
import com.mtl.hulk.sequence.IncrTimeSequence;

import java.util.concurrent.CopyOnWriteArrayList;

public class ReadCommitedExecutor extends HulkMvccExecutor {

    @Override
    public long init(String actionKey, Object args) {
        this.actionKey.set(actionKey);
        long currentVersion = IncrTimeSequence.getInstance(BusinessIdForSequence.TRANSACTION_METHOD_ID.value()).nextId();
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
