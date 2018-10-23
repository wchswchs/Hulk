package com.mtl.hulk.snapshot.rule;

import com.mtl.hulk.common.AutoIncrementGenerator;
import com.mtl.hulk.snapshot.SnapShotHeader;
import com.mtl.hulk.snapshot.SnapShotRule;

import java.io.File;

public class IncrementShardingRule extends SnapShotRule {

    public IncrementShardingRule(int quota) {
        super(quota);
    }

    @Override
    public File run(SnapShotHeader header) {
        File snapshotFile = new File(header.getDir(), header.getFileName()
                                    + "." + AutoIncrementGenerator.getCurrentValue());
        if (snapshotFile.length() >= quota) {
            AutoIncrementGenerator.setCurrentValue(AutoIncrementGenerator.getFactor().incrementAndGet());
            snapshotFile = new File(header.getDir(), header.getFileName()
                                        + "." + AutoIncrementGenerator.getFactor());
        }
        return snapshotFile;
    }

}
