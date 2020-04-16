package com.vx.service.impl;

import com.vx.dao.UserMapper;
import com.vx.exception.VxxcxException;
import com.vx.form.CodeForm;
import com.vx.model.User;
import com.vx.model.VxSessionModel;
import com.vx.service.AnonService;
import com.vx.utils.*;
import com.vx.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zty
 * @date 2020/4/15 下午8:22
 * @description:
 */
@Service
@Slf4j
public class AnonServiceImpl implements AnonService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private UserMapper userMapper;

    @Transactional
    @Override
    public ResultVO vxLogin(CodeForm code, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("参数注意必填项！");
//            throw new VxxcxException(400,bindingResult.getFieldError().getDefaultMessage());
            return ResultVOUtil.error(bindingResult.getFieldError().getDefaultMessage());
        }

        log.info("vx-login-code: " + code.getCode());
        //https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, String> param = new HashMap<>();
        param.put("appid", "wx7e139fc4dec9fd08");
        param.put("secret", "d483c36cf9f93500a17aa0cb788a0f48");
        param.put("js_code", code.getCode());
        param.put("grant_type", "authorization_code");

        String vxResult = HttpClientUtil.doGet(url, param);

        VxSessionModel model = JsonUtils.jsonToPojo(vxResult, VxSessionModel.class);

        //重新登录覆盖redis中的数据
        assert model != null;

        if (!isExistOpenid(model.getOpenid())) {
            User user = new User();
            user.setOpenid(model.getOpenid());
            user.setNowNum(0);
            user.setMaxNum(5);
            user.setCreateTime(DateUtil.getNowDate());
            user.setModifyTime(DateUtil.getNowDate());
            //fanout 广播
            rabbitTemplate.convertAndSend("userInsert","",user);
        }
        //将session存入redis
        redisUtil.set(model.getOpenid(), model.getSession_key(), 60 * 60 * 24 * 3);

        //返回md5加密后的openid
        return ResultVOUtil.success(MD5Util.string2MD5(model.getOpenid()));
    }

    public boolean isExistOpenid(String openid) {
        if (userMapper.selectByOpenid(openid) == null) {
            return false;
        }
        return true;
    }
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,//创建临时队列
                    exchange = @Exchange(value = "userInsert", type = "fanout")
            )
    })
    public void insertUser(User user){
        userMapper.insert(user);
        log.info("插入新用户");
    }



}
