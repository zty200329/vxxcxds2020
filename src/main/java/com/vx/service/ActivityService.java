package com.vx.service;

import com.vx.form.*;
import com.vx.vo.ResultVO;
import org.springframework.validation.BindingResult;
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
     * 差UN个Ian
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


    public ResultVO selectByDistance(ActivityDistanceForm activityDistanceForm,BindingResult bindingResult);
}
