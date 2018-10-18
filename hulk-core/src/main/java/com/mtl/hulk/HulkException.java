package com.mtl.hulk;

public class HulkException {

    private int code;
    private String message;

    public HulkException() {}

    public HulkException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
