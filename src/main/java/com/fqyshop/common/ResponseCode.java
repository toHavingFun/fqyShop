package com.fqyshop.common;

/**
 * Created by Administrator on 2019/1/16.
 */
public enum ResponseCode {
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGMENT");

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
