package com.mtl.hulk.snapshot.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("mtl.hulk.snapshot")
public class SnapshotProperties {

    private String rule = "increment";
    private int bufferSize = 20 * 1024;
    private String dir = "/data/hulk";
    private int perFileSize = 1000;

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }

    public void setPerFileSize(int perFileSize) {
        this.perFileSize = perFileSize;
    }

    public int getPerFileSize() {
        return perFileSize;
    }

}
