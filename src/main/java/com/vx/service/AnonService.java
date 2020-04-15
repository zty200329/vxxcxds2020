package com.vx.service;

import com.vx.form.CodeForm;
import com.vx.vo.ResultVO;
import org.springframework.validation.BindingResult;

/**
 * @author zty
 * @date 2020/4/15 下午7:47
 * @description:
 */
public interface AnonService {
    /**
     * 微信登录接口
     * @param code
     * @param bindingResult
     * @return
     */
    ResultVO vxLogin(CodeForm code, BindingResult bindingResult);
}
