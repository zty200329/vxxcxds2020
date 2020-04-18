package com.vx.service.impl;

import com.vx.dao.ActivityMapper;
import com.vx.dao.OperationMapper;
import com.vx.enums.ResultEnum;
import com.vx.form.ActivityForm;
import com.vx.form.CallNumberForm;
import com.vx.form.JoinSonActivityForm;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

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
    public ResultVO addActivity(ActivityForm activityForm, BindingResult bindingResult) {
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
            BeanUtils.copyProperties(sonActivityForm, operation);
            operationMapper.insert(operation);
        }
        return ResultVOUtil.success();
    }

    @Override
    public ResultVO addPicture(MultipartFile file) {
        if (file.isEmpty()) {
            return ResultVOUtil.error(ResultEnum.IMAGE_IS_NULL);
        }
        return ResultVOUtil.success(UploadImageUtil.uploadFile(file));
    }

    @Override
    public ResultVO joinSonActivity(JoinSonActivityForm joinSonActivityForm, BindingResult bindingresult) {
        if (bindingresult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingresult.getFieldError().getDefaultMessage());
        }
        //登录校验
        if (redisUtil.get(joinSonActivityForm.getOpenId()) == null) {
            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
        }
        //生成排队队列的key
        String setName = joinSonActivityForm.getActivityId() + "+" + joinSonActivityForm.getSonActivityId();
        if (redisUtil.zRank(setName, joinSonActivityForm.getOpenId()) != null) {
            return ResultVOUtil.error(ResultEnum.HAS_IN_QUEUE);
        }


        redisUtil.zAdd(setName, joinSonActivityForm.getOpenId(), System.currentTimeMillis());
        Long test1 = redisUtil.zRank("1+1", "o6Dow");
        log.info(String.valueOf(test1));


        return ResultVOUtil.success();
    }

    @Override
    public ResultVO callNumber( CallNumberForm callNumberForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
        //生成排队队列的key
        String setName = callNumberForm.getActivityId() + "+" + callNumberForm.getSonActivityId();

        //获取前几人
        Set<String> peopleSets = redisUtil.zRange(setName, 0, callNumberForm.getPeopleNum()-1);
        //叫号消息
        //删除
        for (String peopleSet : peopleSets) {
            redisUtil.zRemove(setName,peopleSet);
        }
        
        return null;
    }

    /**
     * 验证权限，暂时不用
     * @param id
     * @param openid
     * @return
     */
    public boolean checkOpenId(Long id,String openid){
        Activity activity = activityMapper.selectByPrimaryKey(id);
        return openid.equals(activity.getOpenid());
    }


}
