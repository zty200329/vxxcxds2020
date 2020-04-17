package com.vx.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @author zty
 * @date 2020/4/16 下午2:07
 * @description:
 */
@Data
public class ActivityForm {

    @NotNull(message = "openidSession不能为空")
    @ApiModelProperty("openidSession")
    private String openIdMd5;

    @NotNull(message = "排队活动名字不能为空 ")
    @ApiModelProperty("排队活动名字")
    private String activityName;

    @NotNull(message = "负责人电话号码不能为空")
    @ApiModelProperty("负责人电话号码")
    private Integer phoneNum;

    @NotNull(message = "活动类型不能为空")
    @ApiModelProperty("活动类型")
    private Integer activityType;

    @ApiModelProperty("位置名称")
    private String name;

    @ApiModelProperty("所在城市")
    private String city;

    @ApiModelProperty("详细地址")
    private String address;

    @ApiModelProperty("纬度")
    private String latitude;

    @ApiModelProperty("经度")
    private String longitude;

    @ApiModelProperty("活动时间")
    private String activityTime;

    @ApiModelProperty("活动描述")
    private String description;

    @NotNull(message = "图片不能为空")
    @ApiModelProperty("图片")
    private MultipartFile file;
}
