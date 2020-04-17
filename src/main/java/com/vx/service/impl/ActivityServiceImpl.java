package com.vx.service.impl;

import com.vx.enums.ResultEnum;
import com.vx.form.ActivityForm;
import com.vx.model.Activity;
import com.vx.service.ActivityService;
import com.vx.utils.RedisUtil;
import com.vx.utils.ResultVOUtil;
import com.vx.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import static com.vx.utils.MD5Util.convertMD5;

/**
 * @author zty
 * @date 2020/4/17 下午2:38
 * @description:
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVO addActivity(ActivityForm activityForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
        if (redisUtil.get(convertMD5(convertMD5(activityForm.getOpenIdMd5()))) == null){
            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
        }
        Activity activity = new Activity();
        BeanUtils.copyProperties(activityForm,activity);

            return null;
    }
}
