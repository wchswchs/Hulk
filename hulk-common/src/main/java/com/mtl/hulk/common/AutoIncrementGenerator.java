package com.mtl.hulk.common;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoIncrementGenerator {

    private final static AtomicInteger factor = new AtomicInteger(1);
    private volatile static Integer currentValue;

    public synchronized static void setCurrentValue(Integer currentValue) {
        AutoIncrementGenerator.currentValue = currentValue;
    }

    public synchronized static Integer getCurrentValue() {
        if (currentValue == null) {
            currentValue = 1;
        }
        return currentValue;
    }

    public static AtomicInteger getFactor() {
        return factor;
    }

}
