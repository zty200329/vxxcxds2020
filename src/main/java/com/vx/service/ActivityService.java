package com.vx.service;

import com.vx.form.ActivityForm;
import com.vx.vo.ResultVO;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;

/**
 * @author zty
 * @date 2020/4/17 下午2:38
 * @description:
 */
public interface ActivityService {
    /**
     * 创建一个活动
     * @param activityForm
     * @param bindingResult
     * @return
     */
    ResultVO addActivity( ActivityForm activityForm, BindingResult bindingResult);
}
