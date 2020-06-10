package com.vx.exception;

import com.vx.enums.ResultEnum;
import lombok.Getter;

/**
 * @author zty
 * @date: 2019/8/12 17:08
 * 描述：
 */
@Getter
public class VxxcxException extends RuntimeException {
    private Integer code;

    public VxxcxException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = resultEnum.getCode();
    }

    public VxxcxException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
