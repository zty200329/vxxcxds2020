package com.vx.service.impl;

import com.vx.dao.ActivityMapper;
import com.vx.dao.OperationMapper;
import com.vx.enums.ResultEnum;
import com.vx.form.ActivityForm;
import com.vx.form.SonActivityForm;
import com.vx.model.Activity;
import com.vx.model.Operation;
import com.vx.service.ActivityService;
import com.vx.utils.RedisUtil;
import com.vx.utils.ResultVOUtil;
import com.vx.utils.UploadImageUtil;
import com.vx.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Autowired
    private OperationMapper operationMapper;
    @Autowired
    private ActivityMapper activityMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVO addActivity(ActivityForm activityForm,BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
        if (redisUtil.get(activityForm.getOpenId()) == null) {
            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
        }

        Activity activity = new Activity();
        activity.setOpenid(activityForm.getOpenId());
        BeanUtils.copyProperties(activityForm, activity);
        activity.setPictureUrl(activityForm.getFileUrl());
        activity.setIsTrue((byte) 1);
        log.info(activity.toString());
        activityMapper.insert(activity);

        return ResultVOUtil.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVO addSonActivity(List<SonActivityForm> sonActivityForms, BindingResult bindingResult) {
        for (SonActivityForm sonActivityForm : sonActivityForms) {
            Operation operation = new Operation();
            BeanUtils.copyProperties(sonActivityForm,operation);
            operationMapper.insert(operation);
        }
        return ResultVOUtil.success();
    }

    @Override
    public ResultVO addPicture(MultipartFile file) {
        if(file.isEmpty()){
            return ResultVOUtil.error(ResultEnum.IMAGE_IS_NULL);
        }
        return ResultVOUtil.success(UploadImageUtil.uploadFile(file));
    }


}
