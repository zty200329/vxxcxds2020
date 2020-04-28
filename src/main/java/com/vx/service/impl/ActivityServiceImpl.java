package com.vx.service.impl;

import com.vx.dao.ActivityMapper;
import com.vx.dao.ActivityUserHistoryMapper;
import com.vx.dao.OperationMapper;
import com.vx.dao.UserMapper;
import com.vx.dto.ActivityDTO;
import com.vx.dto.OperationDTO;
import com.vx.enums.ResultEnum;
import com.vx.form.*;
import com.vx.model.*;
import com.vx.service.ActivityService;
import com.vx.utils.*;
import com.vx.vo.*;
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

import java.util.*;

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
        if (redisUtil.get(activityForm.getOpenId()) == null) {
            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
        }


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
     *
     * @param joinSonActivityForm
     * @param bindingresult
     * @return
     */
    @Override
    public ResultVO joinSonActivity(JoinSonActivityForm joinSonActivityForm, BindingResult bindingresult) {
        if (bindingresult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingresult.getFieldError()).getDefaultMessage());
        }
        //登录校验
        if (redisUtil.get(joinSonActivityForm.getOpenId()) == null) {
            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
        }
        Operation operation = operationMapper.selectByPrimaryKey(joinSonActivityForm.getSonActivityId());

        if (operation.getIsTrue() != 1) {
            return ResultVOUtil.error(ResultEnum.QUEUE_IS_CLOSED);
        }
        //生成排队队列的key
        String setName = joinSonActivityForm.getActivityId() + "+" + joinSonActivityForm.getSonActivityId();
        if (redisUtil.zRank(setName, joinSonActivityForm.getOpenId()) != null) {
            return ResultVOUtil.error(ResultEnum.HAS_IN_QUEUE);
        }
        //fanout 广播
        rabbitTemplate.convertAndSend("insertHistory", "",
                joinSonActivityForm.getOpenId() + "+" + joinSonActivityForm.getActivityId() + "+" + joinSonActivityForm.getSonActivityId());


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
        if (!checkOpenId(callNumberForm.getActivityId(), callNumberForm.getOpenId())) {
            return ResultVOUtil.error(ResultEnum.PERMISSION_DENNY);
        }
        Operation operation = operationMapper.selectByPrimaryKey(callNumberForm.getSonActivityId());

        if (operation.getIsTrue() != 1) {
            return ResultVOUtil.error(ResultEnum.QUEUE_IS_CLOSED);
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
                    peopleSet + "+" + callNumberForm.getActivityId() + "+" + callNumberForm.getSonActivityId());


            //消息队列实现订阅提醒功能，预留接口


            redisUtil.zRemove(setName, peopleSet);
            log.info("移除： " + peopleSet);
        }
        return ResultVOUtil.success(redisUtil.zSize(setName));
    }

    /**
     * 叫号后更新状态
     *
     * @param peopleSet
     */
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
        ActivityUserHistory history = activityUserHistoryMapper.selectByOpenId(getUserIdByOpenId(p1), p2, p3, false);
        history.setIsOk(true);
        activityUserHistoryMapper.updateByPrimaryKey(history);
        log.info("排队完成");
    }

    /**
     * 发送订阅消息
     *
     * @param peopleSet
     */
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange = @Exchange(value = "updateHistory", type = "fanout")
            )
    })
    public void sendMessage(String peopleSet) {
        String[] temp;
        temp = peopleSet.split("\\+");
        String p1 = temp[0];
        Long p2 = Long.valueOf(temp[1]);
        Long p3 = Long.valueOf(temp[2]);
        ActivityUserHistory history = activityUserHistoryMapper.selectByOpenId2(getUserIdByOpenId(p1), p2, p3);
        SubscribeMessageVO bean = new SubscribeMessageVO();
        bean.setThing4(new SubscribeMessageVO.Thing4(history.getActivityName() + ":" + history.getName()));
        bean.setThing6(new SubscribeMessageVO.Thing6(history.getAddress()));
        bean.setThing7(new SubscribeMessageVO.Thing7("请到服务处联系工作人员"));
        WxMssVO wxMssVO = new WxMssVO();
        wxMssVO.setTouser(p1);
        wxMssVO.setTemplate_id("ZpQrMu4Y9fx8xv5qOSsaspeFCCA9RKazzCFQlplB1iA");
        wxMssVO.setData(bean);
        push(wxMssVO);
    }

    public void push(WxMssVO wxMssVO) {
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + getAccessToken();
        String json = JsonUtils.objectToJson(wxMssVO);
        String vxResult = HttpClientUtil.doPostJson(url, json);
        log.info("返回的内容：" + vxResult);
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

    public ResultVO selectByDistance(ActivityDistanceForm activityDistanceForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }
        List<ActivityVO> activityVOS = new LinkedList<>();
        List<ActivityDTO> activityDTOS = new LinkedList<>();
        if (activityDistanceForm.getDistance() == null){
            activityDTOS = activityMapper.selectAsDistance1();

        }else {
            //先计算查询点的经纬度范围
            double r = 6371;//地球半径千米
            double dis = activityDistanceForm.getDistance();//0.5千米距离
            log.info(String.valueOf(dis));
            double dlng = 2 * Math.asin(Math.sin(dis / (2 * r)) / Math.cos(activityDistanceForm.getLatitude() * Math.PI / 180));
            dlng = dlng * 180 / Math.PI;//角度转为弧度
            double dlat = dis / r;
            dlat = dlat * 180 / Math.PI;
            double minlat = activityDistanceForm.getLatitude() - dlat;
            double maxlat = activityDistanceForm.getLatitude() + dlat;
            double minlng = activityDistanceForm.getLongitude() - dlng;
            double maxlng = activityDistanceForm.getLongitude() + dlng;
            activityDTOS = activityMapper.selectAsDistance(minlat, maxlat, minlng, maxlng, dis);
        }
        for (ActivityDTO activityDTO : activityDTOS) {
            ActivityVO activityVO = new ActivityVO();
            BeanUtils.copyProperties(activityDTO, activityVO);
            double distance = LocationUtils.getDistance(activityDistanceForm.getLatitude(), activityDistanceForm.getLongitude(), activityDTO.getLatitude(), activityDTO.getLongitude());
            activityVO.setDistance(distance);
            activityVOS.add(activityVO);
        }

        Collections.sort(activityVOS, new Comparator<ActivityVO>() {
            @Override
            public int compare(ActivityVO o1, ActivityVO o2) {
                return o1.getDistance().compareTo(o2.getDistance());
            }
        });
        return ResultVOUtil.success(activityVOS);
    }


    /**
     * 查出子活动
     *
     * @param id
     * @return
     */
    @Override
    public ResultVO selectByActivityId(Long id) {
        List<OperationDTO> operationDTOS = operationMapper.selectByActivityId(id);
        List<OperationVO> operationVOS = new LinkedList<>();
        for (OperationDTO operationDTO : operationDTOS) {
            String key = operationDTO.getActivityId()+"+"+operationDTO.getId();
            OperationVO operationVO = new OperationVO();
            BeanUtils.copyProperties(operationDTO,operationVO);
            operationVO.setAllPeople(redisUtil.zSize(key));
            operationVOS.add(operationVO);
        }
        return ResultVOUtil.success(operationVOS);
    }

    @Override
    public ResultVO getOwnActivity(String openId) {
        if (redisUtil.get(openId) == null) {
            return ResultVOUtil.error(ResultEnum.USER_NOT_LOGIN);
        }
        return ResultVOUtil.success(activityMapper.selectByOpenid(openId));
    }

    /**
     * 将某一条排队关闭 删除redis排队信息
     *
     * @param joinSonActivityForm
     * @param bindingResult
     * @return
     */
    @Override
    public ResultVO stopOneQueueing(JoinSonActivityForm joinSonActivityForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        if (!checkOpenId(joinSonActivityForm.getActivityId(), joinSonActivityForm.getOpenId())) {
            return ResultVOUtil.error(ResultEnum.PERMISSION_DENNY);
        }
        Operation operation = operationMapper.selectByPrimaryKey(joinSonActivityForm.getSonActivityId());
        operation.setIsTrue((byte) 0);
        operationMapper.updateByPrimaryKey(operation);
        String setName = joinSonActivityForm.getActivityId() + "+" + joinSonActivityForm.getSonActivityId();
        Set<String> sets = redisUtil.zReverseRangeByScore(setName,
                0, System.currentTimeMillis());


        for (String peopleSet : sets) {
            //fanout 广播
            rabbitTemplate.convertAndSend("updateHistory", "",
                    peopleSet + "+" + joinSonActivityForm.getActivityId() + "+" + joinSonActivityForm.getSonActivityId());


            //消息队列实现订阅提醒功能，预留接口


            redisUtil.zRemove(setName, peopleSet);
            log.info("移除： " + peopleSet);
        }
        return ResultVOUtil.success();
    }

    @Override
    public ResultVO pauseQueue(JoinSonActivityForm joinSonActivityForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        if (!checkOpenId(joinSonActivityForm.getActivityId(), joinSonActivityForm.getOpenId())) {
            return ResultVOUtil.error(ResultEnum.PERMISSION_DENNY);
        }
        /**
         * is_true 0为关闭 1 为正常 2为暂停
         */
        Operation operation = operationMapper.selectByPrimaryKey(joinSonActivityForm.getSonActivityId());
        operation.setIsTrue((byte) 2);
        operationMapper.updateByPrimaryKey(operation);
        return ResultVOUtil.success();
    }

    @Override
    public ResultVO restartQueue(JoinSonActivityForm joinSonActivityForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        if (!checkOpenId(joinSonActivityForm.getActivityId(), joinSonActivityForm.getOpenId())) {
            return ResultVOUtil.error(ResultEnum.PERMISSION_DENNY);
        }
        Operation operation = operationMapper.selectByPrimaryKey(joinSonActivityForm.getSonActivityId());
        operation.setIsTrue((byte) 1);
        operationMapper.updateByPrimaryKey(operation);
        return ResultVOUtil.success();
    }

    /**
     * 查看排名
     * @param openid
     * @return
     */
    @Override
    public ResultVO viewMyJoinQueue(String openid) {
        List<viewMyJoinQueueVO> myJoinQueueVOS = new LinkedList<>();
        List<ActivityUserHistory> history = activityUserHistoryMapper.selectByOpenid3(getUserIdByOpenId(openid));
        log.info("长度:"+history.size());
        for (ActivityUserHistory activityUserHistory : history) {
            viewMyJoinQueueVO myJoinQueueVO = new viewMyJoinQueueVO();
            String str = activityUserHistory.getActivityId()+"+"+activityUserHistory.getSonActivityId();
            log.info(activityUserHistory.toString());
            Activity activity = activityMapper.selectByPrimaryKey(activityUserHistory.getActivityId());
            log.info(activity.toString());
            Operation operation = operationMapper.selectByPrimaryKey(activityUserHistory.getSonActivityId());
            log.info(operation.toString());
            myJoinQueueVO.setActivityId(activity.getId());
            myJoinQueueVO.setSonActivityId(operation.getId());
            myJoinQueueVO.setActivityName(activity.getActivityName());
            myJoinQueueVO.setSonActivityName(operation.getName());
            myJoinQueueVO.setRank(redisUtil.zRank(str,openid)+1);
            myJoinQueueVO.setAllPeople(redisUtil.zSize(str));
            myJoinQueueVO.setAddress(activity.getAddress());
            myJoinQueueVO.setDescription(operation.getDescription());
            myJoinQueueVOS.add(myJoinQueueVO);
        }
        return ResultVOUtil.success(myJoinQueueVOS);
    }

    /**
     * 用户退出一条排队
     * @param quitQueueForm
     * @param bindingResult
     * @return
     */
    @Override
    public ResultVO QuitQueue(QuitQueueForm quitQueueForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        String key = quitQueueForm.getActivityId()+"+"+quitQueueForm.getSonActivityId();
        redisUtil.zRemove(key,quitQueueForm.getOpenId());
        ActivityUserHistory history = activityUserHistoryMapper.selectByOpenId(getUserIdByOpenId(quitQueueForm.getOpenId()), quitQueueForm.getActivityId(),quitQueueForm.getSonActivityId(),false);
        activityUserHistoryMapper.deleteByPrimaryKey(history.getId());
        return ResultVOUtil.success();
    }

    @Override
    public ResultVO inquireNum(InquireNumForm inquireNumForm, BindingResult bindingResult) {
        String key = inquireNumForm.getActivityId()+"+"+inquireNumForm.getSonActivityId();
        Long allPeople = redisUtil.zSize(key);
        return ResultVOUtil.success(allPeople);
    }

    @Override
    public ResultVO findStore(FindStoreForm findStoreForm,BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        double r = 6371;//地球半径千米
        List<ActivityDTO> activityDTOS = activityMapper.findStore("%"+findStoreForm.getActivityName()+"%");
        List<ActivityVO> activityVOS = new LinkedList<>();
        for (ActivityDTO activityDTO : activityDTOS) {
            ActivityVO activityVO = new ActivityVO();
            BeanUtils.copyProperties(activityDTO, activityVO);
            double distance = LocationUtils.getDistance(findStoreForm.getLatitude(), findStoreForm.getLongitude(), activityDTO.getLatitude(), activityDTO.getLongitude());
            activityVO.setDistance(distance);
            activityVOS.add(activityVO);
        }
        Collections.sort(activityVOS, new Comparator<ActivityVO>() {
            @Override
            public int compare(ActivityVO o1, ActivityVO o2) {
                return o1.getDistance().compareTo(o2.getDistance());
            }
        });
        return ResultVOUtil.success(activityVOS);
    }

    /**
     * 删除
     * @param deleteActivityForm
     * @param bindingResult
     * @return
     */
    @Override
    public ResultVO deleteActivity(DeleteActivityForm deleteActivityForm,BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        if (!checkOpenId(deleteActivityForm.getActivityId(), deleteActivityForm.getOpenId())) {
            return ResultVOUtil.error(ResultEnum.PERMISSION_DENNY);
        }

        Activity activity = activityMapper.selectByPrimaryKey(deleteActivityForm.getActivityId());
        List<Operation> operationList = operationMapper.selectByActivityId2(activity.getId());
        for (Operation operation : operationList) {
            String key = operation.getActivityId()+"+"+operation.getId();
            log.info(operation.toString());
            if(redisUtil.zSize(key) != 0){
                return ResultVOUtil.error(ResultEnum.THERE_ARE_QUEUES_NOT_CLOSED);
            }
        }
        for (Operation operation : operationList) {
            operation.setIsTrue((byte) 0);
            operationMapper.updateByPrimaryKey(operation);
        }
        activity.setIsTrue((byte) 0);
        activityMapper.updateByPrimaryKey(activity);
        return ResultVOUtil.success();
    }

    @Override
    public ResultVO deleteQueue(DeleteQueueForm deleteQueueForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
            return ResultVOUtil.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        Operation operation = operationMapper.selectByPrimaryKey(deleteQueueForm.getSonActivityId());
        Activity activity = activityMapper.selectByPrimaryKey(operation.getActivityId());
        if (!checkOpenId(operation.getActivityId(), activity.getOpenid())){
            return ResultVOUtil.error(ResultEnum.PERMISSION_DENNY);
        }
        String key = operation.getActivityId()+"+"+operation.getId();
        if(redisUtil.zSize(key) != 0){
            return ResultVOUtil.error(ResultEnum.THERE_ARE_QUEUES_NOT_CLOSED);
        }
        operation.setIsTrue((byte) 0);
        operationMapper.updateByPrimaryKey(operation);
        return ResultVOUtil.success();
    }


    @Override
    public ResultVO viewHistory(String openId) {
        Long userId = getUserIdByOpenId(openId);
        List<ActivityUserHistory> activityUserHistory = activityUserHistoryMapper.selectByUserId(userId);

        for (ActivityUserHistory history : activityUserHistory) {

        }
        return null;
    }


    /**
     * 获取AccessToken
     *
     * @return
     */
    public String getAccessToken() {
        if (redisUtil.get("access_token") != null) {
            return (String) redisUtil.get("access_token");
        }
        //GET https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        Map<String, String> param = new HashMap<>();
        param.put("grant_type", "client_credential");
        param.put("appid", "wx7e139fc4dec9fd08");
        param.put("secret", "d483c36cf9f93500a17aa0cb788a0f48");

        String vxResult = HttpClientUtil.doGet(url, param);
        log.info(vxResult);

        getAccessTokenModel accessTokenModel = JsonUtils.jsonToPojo(vxResult, getAccessTokenModel.class);

        redisUtil.set("access_token", accessTokenModel.getAccess_token(), accessTokenModel.getExpires_in());
        return accessTokenModel.getAccess_token();
        //POST https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=ACCESS_TOKEN
    }


}


