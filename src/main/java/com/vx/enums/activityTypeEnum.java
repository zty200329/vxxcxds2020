package com.vx.enums;

/**
 * @author zty
 * @date 2020/4/21 下午5:58
 * @description:
 */
public enum activityTypeEnum {


    /**
     *
     */

    CATERING(1,"餐饮");



    private Integer code;

    private String msg;

    activityTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
