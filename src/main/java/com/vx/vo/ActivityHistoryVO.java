package com.vx.vo;

import lombok.Data;

/**
 * @author zty
 * @date 2020/4/28 下午3:36
 * @description:
 */
@Data
public class ActivityHistoryVO {
    private Long id;

    private String activityName;

    private String activityType;

    private String name;

    private String address;

    private String activityTime;

    private String description;

    private String pictureUrl;

}
