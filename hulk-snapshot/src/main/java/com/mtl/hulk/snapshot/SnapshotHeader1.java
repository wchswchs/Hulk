package com.mtl.hulk.snapshot;

public class SnapshotHeader1 {

    private String dir;
    private String fileName;

    public SnapshotHeader1(String dir) {
        this.dir = dir;
    }

    public SnapshotHeader1(String dir, String fileName) {
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
