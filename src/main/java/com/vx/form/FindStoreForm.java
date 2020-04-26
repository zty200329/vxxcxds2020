package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/26 下午2:11
 * @description:
 */@Data
public class FindStoreForm {
    @NotNull(message = "排队活动名字不能为空(模糊查询)输入部分 ")
    @ApiModelProperty("排队活动名字")
    private String activityName;

    @ApiModelProperty("纬度")
    @NotNull(message = "纬度信息不能为空")
    private Double latitude;

    @ApiModelProperty("经度")
    @NotNull(message = "经度信息不能为空")
    private Double longitude;

}
