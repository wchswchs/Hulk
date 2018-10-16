package com.mtl.hulk.model;

public enum BusinessIdForSequence {

    TRANSACTION_ID("1"),
    TRANSACTION_METHOD_ID("2");

    private String i;

    private BusinessIdForSequence(String i) {
        this.i = i;
    }

    public String value() {
        return this.i;
    }

}
