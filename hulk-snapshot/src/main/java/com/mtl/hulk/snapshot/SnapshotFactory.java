package com.mtl.hulk.snapshot;

import  com.mtl.hulk.snapshot.conf.SnapshotProperties;
import com.mtl.hulk.snapshot.rule.IncrementShardingRule1;
import com.mtl.hulk.snapshot.rule.Quota;

public class SnapshotFactory {

    private final SnapshotProperties properties;

    public SnapshotFactory(SnapshotProperties properties) {
        this.properties = properties;
    }

    public Snapshot1 createSnapshot(int bufferSize, int perFileSize) {
        SnapshotRule1 rule = selectRule(properties.getRule(), new Quota(bufferSize, perFileSize));
        return new Snapshot1(new SnapshotHeader1(properties.getDir()), rule);
    }

    public Snapshot1 createSnapshot(String header, int bufferSize, int perFileSize) {
        SnapshotRule1 rule = selectRule(properties.getRule(), new Quota(bufferSize, perFileSize));
        SnapshotHeader1 sHeader = new SnapshotHeader1(properties.getDir(), header);

        return new Snapshot1(sHeader, rule);
    }

    private SnapshotRule1 selectRule(String rule, Quota quota) {
        if (rule == "increment") {
            return new IncrementShardingRule1(quota);
        }
        return null;
    }

    public SnapshotProperties getProperties() {
        return properties;
    }

}
