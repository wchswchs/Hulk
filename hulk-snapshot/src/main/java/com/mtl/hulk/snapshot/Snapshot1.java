package com.mtl.hulk.snapshot;

import com.mtl.hulk.serializer.kryo.KryoSerializer;
import com.mtl.hulk.snapshot.io.FastFile;

import java.io.File;
import java.util.List;

public class Snapshot1 {

    private SnapshotHeader1 header;
    private SnapshotRule1 rule;

    public Snapshot1(SnapshotHeader1 header, SnapshotRule1 rule) {
        this.header = header;
        this.rule = rule;
    }

    public SnapshotHeader1 getHeader() {
        return header;
    }

    public void setHeader(SnapshotHeader1 header) {
        this.header = header;
    }

    public SnapshotRule1 getRule() {
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
