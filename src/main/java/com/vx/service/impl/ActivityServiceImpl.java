package com.vx.service.impl;

import com.vx.dao.ActivityMapper;
import com.vx.dao.ActivityUserHistoryMapper;
import com.vx.dao.OperationMapper;
import com.vx.dao.UserMapper;
import com.vx.dto.ActivityDTO;
import com.vx.enums.ResultEnum;
import com.vx.form.*;
import com.vx.model.Activity;
import com.vx.model.ActivityUserHistory;
import com.vx.model.Operation;
import com.vx.model.User;
import com.vx.service.ActivityService;
import com.vx.utils.DateUtil;
import com.vx.utils.RedisUtil;
import com.vx.utils.ResultVOUtil;
import com.vx.utils.UploadImageUtil;
import com.vx.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

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
    private UserMapper userMapper;
    @Autowired
    private OperationMapper operationMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityUserHistoryMapper activityUserHistoryMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVO addActivity(ActivityForm activityForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
//        if (redisUtil.get(activityForm.getOpenId()) == null) {
//            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
//        }

        Activity activity = new Activity();
        activity.setOpenid(activityForm.getOpenId());
        BeanUtils.copyProperties(activityForm, activity);
        activity.setLatitude(Double.valueOf(activityForm.getLatitude()));
        activity.setLongitude(Double.valueOf(activityForm.getLongitude()));
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

    /**
     * 参与排队
     * @param joinSonActivityForm
     * @param bindingresult
     * @return
     */
    @Override
    public ResultVO joinSonActivity(JoinSonActivityForm joinSonActivityForm, BindingResult bindingresult) {
        if (bindingresult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingresult.getFieldError().getDefaultMessage());
        }
        //登录校验
//        if (redisUtil.get(joinSonActivityForm.getOpenId()) == null) {
//            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
//        }
        //生成排队队列的key
        String setName = joinSonActivityForm.getActivityId() + "+" + joinSonActivityForm.getSonActivityId();
        if (redisUtil.zRank(setName, joinSonActivityForm.getOpenId()) != null) {
            return ResultVOUtil.error(ResultEnum.HAS_IN_QUEUE);
        }
        //fanout 广播
        rabbitTemplate.convertAndSend("insertHistory", "",
                joinSonActivityForm.getOpenId()+"+"+joinSonActivityForm.getActivityId()+"+"+joinSonActivityForm.getSonActivityId());


        redisUtil.zAdd(setName, joinSonActivityForm.getOpenId(), System.currentTimeMillis());

        //获取前面还有多少个
//        Long test1 = redisUtil.zRank("1+1", "o6Dow");
//        log.info(String.valueOf(test1));


        return ResultVOUtil.success();
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange = @Exchange(value = "insertHistory", type = "fanout")
            )
    })
    public void insertHistory(String peopleSet) {
        String[] temp;
        temp = peopleSet.split("\\+");
        String p1 = temp[0];
        Long p2 = Long.valueOf(temp[1]);
        Long p3 = Long.valueOf(temp[2]);
        Activity activity = activityMapper.selectByPrimaryKey(p2);
        Operation operation = operationMapper.selectByPrimaryKey(p3);

        ActivityUserHistory history = new ActivityUserHistory();
        history.setActivityName(activity.getActivityName());
        history.setName(operation.getName());
        history.setAddress(activity.getAddress());
        history.setActivityId(p2);
        history.setSonActivityId(p3);
        history.setTime(DateUtil.getNowDate());
        history.setUserId(getUserIdByOpenId(p1));
        history.setIsOk(false);

        activityUserHistoryMapper.insert(history);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVO callNumber(CallNumberForm callNumberForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
        //生成排队队列的key
        String setName = callNumberForm.getActivityId() + "+" + callNumberForm.getSonActivityId();

        //获取前几人
        Set<String> peopleSets = redisUtil.zRange(setName, 0, callNumberForm.getPeopleNum() - 1);
        log.info(String.valueOf(peopleSets.size()));
        //叫号消息
        //删除
        for (String peopleSet : peopleSets) {
            //fanout 广播
            rabbitTemplate.convertAndSend("updateHistory", "",
                    peopleSet+"+"+callNumberForm.getActivityId()+"+"+callNumberForm.getSonActivityId());


            //消息队列实现订阅提醒功能，预留接口
            redisUtil.zRemove(setName, peopleSet);
            log.info("移除： " + peopleSet);
        }
        return ResultVOUtil.success();
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange = @Exchange(value = "updateHistory", type = "fanout")
            )
    })
    public void updateHistory(String peopleSet) {
        String[] temp;
        temp = peopleSet.split("\\+");
        String p1 = temp[0];
        Long p2 = Long.valueOf(temp[1]);
        Long p3 = Long.valueOf(temp[2]);
        log.info(p1);
        ActivityUserHistory history = activityUserHistoryMapper.selectByOpenId(getUserIdByOpenId(p1),p2,p3,false);
        history.setIsOk(true);
        activityUserHistoryMapper.updateByPrimaryKey(history);
        log.info("排队完成");
    }

    public static void main(String[] args) {
        String peopleSet = "asdfasfasf+ssss+asfas";
        String[] temp;
        temp = peopleSet.split("\\+");
        String p = temp[0];
        System.out.println(p);
    }
    public Long getUserIdByOpenId(String openId) {
        User user = userMapper.selectByOpenid(openId);
        return user.getId();
    }

    /**
     * 验证权限，暂时不用
     *
     * @param id
     * @param openid
     * @return
     */
    public boolean checkOpenId(Long id, String openid) {
        Activity activity = activityMapper.selectByPrimaryKey(id);
        return openid.equals(activity.getOpenid());
    }

    public ResultVO selectByDistance(ActivityDistanceForm activityDistanceForm,BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
        //先计算查询点的经纬度范围
        double r = 6371;//地球半径千米
        double dis = activityDistanceForm.getDistance();//0.5千米距离
        double dlng =  2*Math.asin(Math.sin(dis/(2*r))/Math.cos(activityDistanceForm.getLatitude()*Math.PI/180));
        dlng = dlng*180/Math.PI;//角度转为弧度
        double dlat = dis/r;
        dlat = dlat*180/Math.PI;
        double minlat =activityDistanceForm.getLatitude()-dlat;
        double maxlat = activityDistanceForm.getLatitude()+dlat;
        double minlng = activityDistanceForm.getLongitude() -dlng;
        double maxlng = activityDistanceForm.getLongitude() + dlng;
//        List<ActivityDTO> activityVOS = activityMapper.selectAsDistance(minlat,maxlat,minlng,maxlng);
//        return ResultVOUtil.success(activityVOS);
        return null;
    }


}
