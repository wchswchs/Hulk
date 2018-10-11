package com.mtl.hulk.exception;

public class HulkException extends RuntimeException {

    private String action;

    public HulkException(String action, Throwable cause) {
        super(cause);
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
