package com.mtl.hulk.common;

import java.util.concurrent.atomic.LongAdder;

public class AutoIncrementGenerator {

    private final static LongAdder factor = new LongAdder();
    private volatile static Integer currentValue;

    public static void setCurrentValue(Integer currentValue) {
        AutoIncrementGenerator.currentValue = currentValue;
    }

    public static Integer getCurrentValue() {
        if (currentValue == null) {
            currentValue = 0;
        }
        return currentValue;
    }

    public static LongAdder getFactor() {
        return factor;
    }

}
