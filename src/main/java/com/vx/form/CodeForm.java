package com.vx.form;

import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zty
 * @date 2020/4/15 下午8:14
 * @description:
 */
@Data
public class CodeForm implements Serializable {
    @NotNull(message = "code不能为空")
    String code;
}
