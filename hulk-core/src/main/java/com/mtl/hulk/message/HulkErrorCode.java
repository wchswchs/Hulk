package com.mtl.hulk.message;

public enum HulkErrorCode {

    COMMIT_FAIL(100, "Commit失败, txId:{0}, action:{1}"),
    ROLLBACK_FAIL(101, "Rollback失败, txId:{0}, action:{1}"),
    TRY_FAIL(102, "Try失败, txId:{0}, action:{1}"),
    COMMIT_TIMEOUT(103, "Commit超时"),
    INTERRUPTED(104, "中断"),
    RUN_EXCEPTION(105, "事务执行异常"),
    REJECT_EXCEPTION(106, "事务执行超载");

    private final int code;

    private final String message;

    private HulkErrorCode(int code, String message) {
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
