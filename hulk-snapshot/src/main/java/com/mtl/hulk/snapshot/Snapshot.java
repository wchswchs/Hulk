package com.mtl.hulk.snapshot;

import com.mtl.hulk.serializer.kryo.KryoSerializer;
import com.mtl.hulk.snapshot.io.FastFile;

import java.io.File;
import java.util.List;

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

    public boolean write(Object data) {
        FastFile ff = null;
        try {
            File file = rule.run(header);
            ff = new FastFile(file, "rw", rule.getQuota().getBufferSize());
            KryoSerializer serializer = new KryoSerializer();
            ff.write(serializer.serialize(data));
            return true;
        } catch (Exception ex) {
            throw ex;
        } finally {
            ff.close();
        }
    }

    public <T> List<T> read(File file, Class<T> targetClass) throws Exception {
        FastFile ff = null;
        try {
            ff = new FastFile(file, "r", rule.getQuota().getBufferSize());
            return ff.read(new KryoSerializer(), targetClass);
        } catch (Exception ex) {
            throw ex;
        } finally {
            ff.close();
        }
    }

}
