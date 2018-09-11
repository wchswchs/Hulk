package com.mtl.hulk.logger.data.sql;

public enum SqlType {

    SELECT(0), INSERT(1), UPDATE(2),
    DELETE(3), SELECT_FOR_UPDATE(4),
    DEFAULT_SQL_TYPE(-100);

    private int i;

    private SqlType(int i) {
        this.i = i;
    }

    public int value() {
        return this.i;
    }

    public static SqlType valueOf(int i) {
        for (SqlType t : values()) {
            if (t.value() == i) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid SqlType:" + i);
    }

}
