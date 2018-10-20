package com.mtl.hulk.model;

public enum BusinessActivityExecutionType {

    TRY(1),
    COMMIT(2),
    ROLLBACK(3);

    private int i;

    private BusinessActivityExecutionType(int i) {
        this.i = i;
    }

    public int value() {
        return this.i;
    }

}
