package com.mtl.hulk.model;

public enum BusinessActivityExecutionType {

    COMMIT("1"),
    ROLLBACK("2");

    private String i;

    private BusinessActivityExecutionType(String i) {
        this.i = i;
    }

    public String value() {
        return this.i;
    }

}
