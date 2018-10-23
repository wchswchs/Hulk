package com.mtl.hulk.snapshot;

import com.mtl.hulk.snapshot.conf.SnapShotProperties;
import com.mtl.hulk.snapshot.rule.IncrementShardingRule;

public class SnapShot {

    private SnapShotProperties properties;

    public SnapShot(SnapShotProperties properties) {
        this.properties = properties;
    }

    public SnapShotRule getRule() {
        if (properties.getRule() == "increment") {
            return new IncrementShardingRule(properties.getBufferSize() * properties.getPerFileSize());
        }
        return null;
    }

    public SnapShotProperties getProperties() {
        return properties;
    }

}
