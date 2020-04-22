package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/21 下午8:13
 * @description:
 */
@Data
public class ActivityDistanceForm {
    @ApiModelProperty("纬度")
    @NotNull(message = "纬度信息不能为空")
    private Double latitude;

    @ApiModelProperty("经度")
    @NotNull(message = "经度信息不能为空")
    private Double longitude;

    @ApiModelProperty("范围")
    private Integer distance;
}
