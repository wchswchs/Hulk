package com.mtl.hulk.snapshot;

public class SnapShotHeader {

    private String dir;
    private String fileName;

    public SnapShotHeader(String dir, String fileName) {
        this.dir = dir;
        this.fileName = fileName;
    }

    public String getDir() {
        return dir;
    }

    public String getFileName() {
        return fileName;
    }

}
