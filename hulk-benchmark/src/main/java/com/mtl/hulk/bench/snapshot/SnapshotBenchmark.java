package com.mtl.hulk.bench.snapshot;

import com.mtl.hulk.bench.AbstractBenchmark;
import com.mtl.hulk.bench.model.OrderEntry;
import com.mtl.hulk.snapshot.Snapshot;
import com.mtl.hulk.snapshot.SnapshotHeader;
import com.mtl.hulk.snapshot.SnapshotRule;
import com.mtl.hulk.snapshot.rule.IncrementShardingRule;
import com.mtl.hulk.snapshot.rule.Quota;
import org.openjdk.jmh.annotations.*;

public class SnapshotBenchmark extends AbstractBenchmark {

    @Benchmark
    public void writeSnapshot() {
        SnapshotHeader header = new SnapshotHeader("/data/hulk", "snapshot.test.log");
        SnapshotRule rule = new IncrementShardingRule(new Quota(10 * 1024, 1000));
        Snapshot snapshot = new Snapshot(header, rule);
        snapshot.write(new OrderEntry("12345"));
    }

}
