package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/26 上午11:21
 * @description:
 */
@Data
public class InquireNumForm {
    @ApiModelProperty("大活动的主键")
    @NotNull(message = "不能为空")
    private Long activityId;

    @ApiModelProperty("排队id")
    @NotNull(message = "排队id不能为空")
    private Long sonActivityId;
}
