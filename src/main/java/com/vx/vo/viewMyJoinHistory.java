package com.vx.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author zty
 * @date 2020/4/28 下午12:32
 * @description:
 */
@Data
public class viewMyJoinHistory {

    private Long sonActivityId;

    private Long activityId;

    //活动名
    private String activityName;

    //排队名
    private String name;

    //类型
    private String Type;

    private String address;

    private Date time;

}
