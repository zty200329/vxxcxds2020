package com.vx.vo;

import lombok.Data;

/**
 * @author zty
 * @date 2020/4/24 下午8:29
 * @description:
 */
@Data
public class viewMyJoinQueueVO {


    private String activityName;

    /**
     * 排队名
     */
    private String sonActivityName;

    private Long rank;

    private Long allPeople;
    /**
     * 地址
     */
    private String address;



    private String description;

}
