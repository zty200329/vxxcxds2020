package com.vx.service;

import com.vx.form.*;
import com.vx.vo.ResultVO;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

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
    ResultVO addActivity(ActivityForm activityForm, BindingResult bindingResult);


    /**
     * @param sonActivityForms
     * @param bindingResult
     * @return
     */
    ResultVO addSonActivity(List<SonActivityForm> sonActivityForms, BindingResult bindingResult);

    ResultVO addPicture(MultipartFile file);

    /**
     * 根据两个主键来加入排队
     * @param joinSonActivityForm
     * @return
     */
    ResultVO joinSonActivity(JoinSonActivityForm joinSonActivityForm, BindingResult bindingresult);

    /**
     * 叫号
     * @param callNumberForm
     * @param bindingResult
     * @return
     */
    ResultVO callNumber(CallNumberForm callNumberForm, BindingResult bindingResult);


    ResultVO selectByDistance(ActivityDistanceForm activityDistanceForm,BindingResult bindingResult);

    ResultVO selectByActivityId(Long id);

    ResultVO getOwnActivity(String openId);

    /**
     * 关闭某条排队 会一次性叫完号
     * @param joinSonActivityForm
     * @param bindingResult
     * @return
     */
    ResultVO stopOneQueueing(JoinSonActivityForm joinSonActivityForm,BindingResult bindingResult);


    /**
     * 暂停排号,但不叫号,叫号分离
     * @param joinSonActivityForm
     * @param bindingResult
     * @return
     */
    ResultVO pauseQueue(JoinSonActivityForm joinSonActivityForm,BindingResult bindingResult);

    /**
     * 重新开启排队
     * @param joinSonActivityForm
     * @param bindingResult
     * @return
     */
    ResultVO restartQueue(JoinSonActivityForm joinSonActivityForm ,BindingResult bindingResult);

    /**
     * 获取正在排队的及前面有多少人
     * @param openid
     * @return
     */
    ResultVO viewMyJoinQueue(String openid);
}
