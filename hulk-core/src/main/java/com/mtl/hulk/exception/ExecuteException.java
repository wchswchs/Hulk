package com.mtl.hulk.exception;

public class ExecuteException extends RuntimeException {

    private String action;

    public ExecuteException(String action, Throwable cause) {
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
