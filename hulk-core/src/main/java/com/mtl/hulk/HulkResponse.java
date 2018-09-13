package com.mtl.hulk;

public class HulkResponse {

    private int status;
    private String message;
    private HulkException exception;


    public HulkResponse(int status, String message, HulkException ex) {
        this.status = status;
        this.message = message;
        this.exception = ex;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public HulkException getException() {
        return exception;
    }

}
