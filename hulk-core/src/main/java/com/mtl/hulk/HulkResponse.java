package com.mtl.hulk;

public class HulkResponse {

    private int code;
    private String message;

    public HulkResponse(int code, String message) {
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
