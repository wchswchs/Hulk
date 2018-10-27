package com.mtl.hulk.bench;

import com.mtl.hulk.tools.SystemPropertyUtil;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all JMH benchmarks.
 */
@Warmup(iterations = AbstractBenchmarkBase.DEFAULT_WARMUP_ITERATIONS)
@Measurement(iterations = AbstractBenchmarkBase.DEFAULT_MEASURE_ITERATIONS)
@State(Scope.Thread)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.SECONDS)
public abstract class AbstractBenchmarkBase {

    protected static final int DEFAULT_WARMUP_ITERATIONS = 5;
    protected static final int DEFAULT_MEASURE_ITERATIONS = 5;
    protected static final String[] BASE_JVM_ARGS = {
        "-server", "-dsa", "-da",
        "-XX:+HeapDumpOnOutOfMemoryError"};

//    static {
//        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
//    }

    protected ChainedOptionsBuilder newOptionsBuilder() throws Exception {
        String className = getClass().getSimpleName();

        ChainedOptionsBuilder runnerOptions = new OptionsBuilder()
            .include(".*" + className + ".*")
            .jvmArgs(jvmArgs());

        if (getWarmupIterations() > 0) {
            runnerOptions.warmupIterations(getWarmupIterations());
        }

        if (getMeasureIterations() > 0) {
            runnerOptions.measurementIterations(getMeasureIterations());
        }

        if (getReportDir() != null) {
            String filePath = getReportDir() + className + ".json";
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            runnerOptions.resultFormat(ResultFormatType.JSON);
            runnerOptions.result(filePath);
        }

        return runnerOptions;
    }

    protected abstract String[] jvmArgs();

    protected static String[] removeAssertions(String[] jvmArgs) {
        List<String> customArgs = new ArrayList<String>(jvmArgs.length);
        for (String arg : jvmArgs) {
            if (!arg.startsWith("-ea")) {
                customArgs.add(arg);
            }
        }
        if (jvmArgs.length != customArgs.size()) {
            jvmArgs = customArgs.toArray(new String[0]);
        }
        return jvmArgs;
    }

    @Test
    public void run() throws Exception {
        Collection<RunResult> rets = new Runner(newOptionsBuilder().build()).run();
        for (RunResult ret : rets) {
            for (BenchmarkResult br : ret.getBenchmarkResults()) {
                if (br.getParams().getMode() == Mode.AverageTime) {
                    Assert.assertTrue("Run time too long", br.getPrimaryResult().getScore() > (double)getTimeThreshold());
                }
            }
        }
    }

    protected int getWarmupIterations() {
        return SystemPropertyUtil.getInt("warmupIterations", -1);
    }

    protected int getMeasureIterations() {
        return SystemPropertyUtil.getInt("measureIterations", -1);
    }

    protected String getReportDir() {
        return SystemPropertyUtil.get("perfReportDir");
    }

    protected int getTimeThreshold() {
        return SystemPropertyUtil.getInt("runLimitTime", 2);
    }

    public static void handleUnexpectedException(Throwable t) {
        Assert.assertNull(t);
    }

}
