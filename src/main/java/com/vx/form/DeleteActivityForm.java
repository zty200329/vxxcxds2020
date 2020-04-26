package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/26 下午8:55
 * @description:
 */
@Data
public class DeleteActivityForm {
    @ApiModelProperty("openid")
    @NotNull(message = "openid不能为空")
    private String openId;

    @ApiModelProperty("大活动的主键")
    @NotNull(message = "不能为空")
    private Long activityId;

}
