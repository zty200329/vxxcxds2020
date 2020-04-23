package com.vx.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zty
 * @date 2020/4/23 下午8:31
 * @description:
 */
@Data
public class SubscribeMessageVO {
    //具体的订阅消息的key {{thing4.DATA}} 则key为thing4
    private Thing4 thing4;
    private Thing6 thing6;
    private Thing7 thing7;
    @Data
    @AllArgsConstructor
    public static class Thing4{
        private String value;
    }
    @Data
    @AllArgsConstructor
    public static class Thing6{
        private String value;
    }
    @Data
    @AllArgsConstructor
    public static class Thing7{
        private String value;
    }
}
