package com.mtl.hulk.snapshot.rule;

import com.mtl.hulk.common.AutoIncrementGenerator;
import com.mtl.hulk.snapshot.SnapshotHeader;
import com.mtl.hulk.snapshot.SnapshotRule;

import java.io.File;

public class IncrementShardingRule extends SnapshotRule {

    public IncrementShardingRule() {
        super();
    }

    public IncrementShardingRule(Quota quota) {
        super(quota);
    }

    @Override
    public File run(SnapshotHeader header) {
        File snapshotFile = new File(header.getDir(), header.getFileName()
                                    + "." + AutoIncrementGenerator.getCurrentValue());
        if (snapshotFile.length() >= (quota.getBufferSize() * quota.getPerFileSize())) {
            AutoIncrementGenerator.getFactor().increment();
            AutoIncrementGenerator.setCurrentValue(AutoIncrementGenerator.getFactor().intValue());
            snapshotFile = new File(header.getDir(), header.getFileName()
                                        + "." + AutoIncrementGenerator.getFactor());
        }
        return snapshotFile;
    }

}
