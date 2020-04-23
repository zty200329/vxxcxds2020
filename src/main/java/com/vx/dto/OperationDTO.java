package com.vx.dto;

import lombok.Data;

/**
 * @author zty
 * @date 2020/4/23 下午12:50
 * @description:
 */
@Data
public class OperationDTO {
    private Long id;

    private Long activityId;

    private String name;

    private String description;
}
