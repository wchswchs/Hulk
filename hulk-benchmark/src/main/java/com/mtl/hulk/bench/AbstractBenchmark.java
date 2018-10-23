package com.mtl.hulk.bench;

import com.mtl.hulk.tools.SystemPropertyUtil;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

/**
 * Default implementation of the JMH microbenchmark adapter.
 */
@Fork(AbstractBenchmark.DEFAULT_FORKS)
public class AbstractBenchmark extends AbstractBenchmarkBase {

    protected static final int DEFAULT_FORKS = 2;
    private final String[] jvmArgs;

    public AbstractBenchmark() {
        this(false);
    }

    public AbstractBenchmark(boolean disableAssertions) {
        final String[] customArgs;

        customArgs = new String[]{"-Xms768m", "-Xmx768m", "-XX:MaxDirectMemorySize=768m"};
        String[] jvmArgs = new String[BASE_JVM_ARGS.length + customArgs.length];
        System.arraycopy(BASE_JVM_ARGS, 0, jvmArgs, 0, BASE_JVM_ARGS.length);
        System.arraycopy(customArgs, 0, jvmArgs, BASE_JVM_ARGS.length, customArgs.length);

        if (disableAssertions) {
            jvmArgs = removeAssertions(jvmArgs);
        }

        this.jvmArgs = jvmArgs;
    }

    @Override
    protected String[] jvmArgs() {
        return jvmArgs;
    }

    @Override
    protected ChainedOptionsBuilder newOptionsBuilder() throws Exception {
        ChainedOptionsBuilder runnerOptions = super.newOptionsBuilder();
        if (getForks() > 0) {
            runnerOptions.forks(getForks());
        }
        return runnerOptions;
    }

    protected int getForks() {
        return SystemPropertyUtil.getInt("forks", -1);
    }

}
