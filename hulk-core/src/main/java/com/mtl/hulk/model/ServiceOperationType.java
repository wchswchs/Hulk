package com.mtl.hulk.model;

public enum ServiceOperationType {

    TCC(1),
    Compensated(2),
    Idempotent(3);

    private final int type;

    private ServiceOperationType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
