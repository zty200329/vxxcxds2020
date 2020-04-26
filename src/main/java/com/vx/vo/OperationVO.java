package com.vx.vo;

import lombok.Data;

/**
 * @author zty
 * @date 2020/4/26 上午9:47
 * @description:
 */
@Data
public class OperationVO {
    private Long id;

    private Long activityId;

    private String name;

    private Long allPeople;

    private String description;

    private Byte isTrue;
}
