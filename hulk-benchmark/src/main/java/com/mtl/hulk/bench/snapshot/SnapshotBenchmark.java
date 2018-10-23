package com.mtl.hulk.bench.snapshot;

import com.mtl.hulk.bench.AbstractBenchmark;
import com.mtl.hulk.bench.model.OrderEntry;
import com.mtl.hulk.serializer.kryo.KryoSerializer;
import com.mtl.hulk.snapshot.io.FastFile;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Fork(2)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SnapshotBenchmark extends AbstractBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.All)
    public void writeSnapshots() {
        FastFile file = new FastFile(new File("/data/hulk", "snapshot.test.log"), "rw", 10 * 1024);
        KryoSerializer serializer = new KryoSerializer();
        byte[] data = serializer.serialize(new OrderEntry("123456"));
        file.write(data);
        file.close();
    }

}
