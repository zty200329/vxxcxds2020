package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/26 下午9:39
 * @description:
 */
@Data
public class DeleteQueueForm {
    @ApiModelProperty("openid")
    @NotNull(message = "openid不能为空")
    private String openId;

    @ApiModelProperty("排队id")
    @NotNull(message = "排队id不能为空")
    private Long sonActivityId;
}
