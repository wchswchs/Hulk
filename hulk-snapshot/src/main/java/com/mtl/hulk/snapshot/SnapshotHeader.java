package com.mtl.hulk.snapshot;

public class SnapshotHeader {

    private String dir;
    private String fileName;

    public SnapshotHeader(String dir) {
        this.dir = dir;
    }

    public SnapshotHeader(String dir, String fileName) {
        this.dir = dir;
        this.fileName = fileName;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
