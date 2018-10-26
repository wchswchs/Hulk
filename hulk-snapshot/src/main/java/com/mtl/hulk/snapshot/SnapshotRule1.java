package com.mtl.hulk.snapshot;

import com.mtl.hulk.snapshot.rule.Quota;

import java.io.File;

public abstract class SnapshotRule1 {

    protected Quota quota;

    public SnapshotRule1() {
    }

    public SnapshotRule1(Quota quota) {
        this.quota = quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    public Quota getQuota() {
        return quota;
    }

    public abstract File run(SnapshotHeader1 header);

}
