package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/18 下午3:42
 * @description:
 */
@Data
public class CallNumberForm {
    @ApiModelProperty("openid")
    @NotNull(message = "openid不能为空")
    private String openId;

    @ApiModelProperty("大活动的主键")
    @NotNull(message = "不能为空")
    private Long activityId;

    @ApiModelProperty("排队id")
    @NotNull(message = "排队id不能为空")
    private Long sonActivityId;

    @ApiModelProperty("叫号人数")
    @NotNull(message = "叫号人数不能为空")
    private Integer peopleNum;
}
