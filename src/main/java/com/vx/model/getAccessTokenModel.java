package com.vx.model;

import lombok.Data;

/**
 * @author zty
 * @date 2020/4/23 下午2:26
 * @description:
 */
@Data
public class getAccessTokenModel {
    private String access_token;
    private Integer expires_in;
}
