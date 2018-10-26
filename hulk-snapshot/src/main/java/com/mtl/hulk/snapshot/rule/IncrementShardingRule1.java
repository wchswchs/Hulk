package com.mtl.hulk.snapshot.rule;

import com.mtl.hulk.common.AutoIncrementGenerator;
import com.mtl.hulk.snapshot.SnapshotHeader1;
import com.mtl.hulk.snapshot.SnapshotRule1;

import java.io.File;

public class IncrementShardingRule1 extends SnapshotRule1 {

    public IncrementShardingRule1() {
        super();
    }

    public IncrementShardingRule1(Quota quota) {
        super(quota);
    }

    @Override
    public File run(SnapshotHeader1 header) {
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
