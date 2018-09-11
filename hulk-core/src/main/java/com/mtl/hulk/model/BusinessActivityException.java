package com.mtl.hulk.model;

public class BusinessActivityException {

    private BusinessActivityId id;
    private String exception;

    public void setId(BusinessActivityId id) {
        this.id = id;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public BusinessActivityId getId() {
        return id;
    }

    public String getException() {
        return exception;
    }
}
