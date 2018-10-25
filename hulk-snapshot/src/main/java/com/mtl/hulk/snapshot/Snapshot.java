package com.mtl.hulk.snapshot;

public class Snapshot {

    private SnapshotHeader header;
    private SnapshotRule rule;

    public Snapshot(SnapshotHeader header, SnapshotRule rule) {
        this.header = header;
        this.rule = rule;
    }

    public SnapshotHeader getHeader() {
        return header;
    }

    public void setHeader(SnapshotHeader header) {
        this.header = header;
    }

    public SnapshotRule getRule() {
        return rule;
    }

}
