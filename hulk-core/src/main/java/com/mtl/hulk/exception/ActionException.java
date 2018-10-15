package com.mtl.hulk.exception;

public class ActionException extends RuntimeException {

    private String action;

    public ActionException(String action, Throwable cause) {
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
