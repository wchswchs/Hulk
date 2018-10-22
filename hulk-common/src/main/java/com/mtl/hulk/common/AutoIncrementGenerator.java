package com.mtl.hulk.common;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoIncrementGenerator {

    private final static AtomicInteger factor = new AtomicInteger(1);

    public static Integer getFactor() {
        return factor.get();
    }

    public static void setFactor(Integer val) {
        factor.set(val);
    }

    public static Integer incrementAndGet() {
        return factor.incrementAndGet();
    }

}
