package com.mtl.hulk.snapshot.rule;

public class Quota {

    private int bufferSize;
    private int perFileSize;

    public Quota(int bufferSize, int perFileSize) {
        this.bufferSize = bufferSize;
        this.perFileSize = perFileSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getPerFileSize() {
        return perFileSize;
    }

}
