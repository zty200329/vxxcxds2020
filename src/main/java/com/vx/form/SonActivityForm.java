package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/17 下午4:42
 * @description:
 */
@Data
public class SonActivityForm {
    @NotNull(message = "主活动id不能为空")
    @ApiModelProperty("主活动id")
    private Long activityId;

    @NotNull(message = "子活动名称不能为空")
    @ApiModelProperty("子活动名称")
    private String name;

    @NotNull(message = "活动描述不能为空")
    @ApiModelProperty("活动描述")
    private String description;
}
