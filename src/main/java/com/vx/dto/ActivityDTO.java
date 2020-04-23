package com.vx.dto;

import lombok.Data;

/**
 * @author zty
 * @date 2020/4/22 下午5:49
 * @description:
 */
@Data
public class ActivityDTO {
    private Long id;

    private String activityName;

    private String phoneNum;

    private Integer activityType;

    private String name;

    private String city;

    private String address;

    private Double latitude;

    private Double longitude;

    private String activityTime;

    private String description;

    private String pictureUrl;

}
