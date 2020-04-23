package com.vx.vo;

import lombok.Data;

/**
 * @author zty
 * @date 2020/4/21 下午5:40
 * @description: 返回店家
 */
@Data
public class ActivityVO {
    private Long id;

    private String activityName;

    private String phoneNum;

    private Integer activityType;

    private String name;

    private String city;

    private String address;

    private String activityTime;

    private String description;

    private String pictureUrl;

    private Double distance;
}
