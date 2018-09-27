package com.mtl.hulk.model;

public enum BusinessActivityIsolationLevel {

    READ_UNCOMMITTED(1),
    Read_COMMITED(2),
    REPEATABLE_READ(3),
    SERIALIZABLE(4);

    private int i;

    private BusinessActivityIsolationLevel(int i) {
        this.i = i;
    }

    public int value() {
        return this.i;
    }

    public static BusinessActivityIsolationLevel valueOf(int i) {
        for (BusinessActivityIsolationLevel t : values()) {
            if (t.value() == i) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid Isolation Level:" + i);
    }

}
