package com.mtl.hulk.snapshot;

import java.io.File;

public abstract class SnapShotRule {

    protected int quota;

    public SnapShotRule(int quota) {
        this.quota = quota;
    }

    public abstract File run(SnapShotHeader header);

}
