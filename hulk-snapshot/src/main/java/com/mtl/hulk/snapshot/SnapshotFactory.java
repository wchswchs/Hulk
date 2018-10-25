package com.mtl.hulk.snapshot;

import com.mtl.hulk.snapshot.conf.SnapshotProperties;
import com.mtl.hulk.snapshot.rule.IncrementShardingRule;
import com.mtl.hulk.snapshot.rule.Quota;

public class SnapshotFactory {

    private final SnapshotProperties properties;

    public SnapshotFactory(SnapshotProperties properties) {
        this.properties = properties;
    }

    public Snapshot createSnapshot(int bufferSize, int perFileSize) {
        SnapshotRule rule = selectRule(properties.getRule(), new Quota(bufferSize, perFileSize));
        return new Snapshot(new SnapshotHeader(properties.getDir()), rule);
    }

    public Snapshot createSnapshot(String header, int bufferSize, int perFileSize) {
        SnapshotRule rule = selectRule(properties.getRule(), new Quota(bufferSize, perFileSize));
        SnapshotHeader sHeader = new SnapshotHeader(properties.getDir(), header);

        return new Snapshot(sHeader, rule);
    }

    private SnapshotRule selectRule(String rule, Quota quota) {
        if (rule == "increment") {
            return new IncrementShardingRule(quota);
        }
        return null;
    }

    public SnapshotProperties getProperties() {
        return properties;
    }

}
