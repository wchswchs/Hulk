package com.mtl.hulk.model;

public enum BusinessActivityStatus {

    TRYING(1, "try阶段"),

    TRYING_EXPT(2, "try失败"),

    TRIED(3, "try成功"),

    COMMITTING(4, "commit阶段"),

    COMMITING_FAILED(5, "commit失败"),

    COMMITTED(6, "commit成功"),

    ROLLBACKING(7, "rollback阶段"),

    ROLLBACKING_FAILED(8, "rollback失败"),

    ROLLBACKED(9, "rollback成功"),

    COMPLETE(10, "业务活动结束");

    private final int code;

    private final String desc;

    private BusinessActivityStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BusinessActivityStatus getBusinessActivityStatus(int code){
        for (BusinessActivityStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
