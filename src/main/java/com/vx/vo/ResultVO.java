package com.vx.vo;

import lombok.Data;

/**
 * @Author: zty
 * @Date: 2020/1/7 18:19
 */
@Data
public class ResultVO<T> {

    private Integer code;

    private String msg;

    private T data;
}
