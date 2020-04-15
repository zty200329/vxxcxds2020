package com.vx.controller;

import com.vx.model.VxSessionModel;
import com.vx.utils.HttpClientUtil;
import com.vx.utils.JsonUtils;
import com.vx.utils.RedisUtil;
import com.vx.utils.ResultVOUtil;
import com.vx.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

/**
 * @author zty
 * @date 2020/4/15 下午1:49
 * @description: 微信登录
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/anon")
@Api(tags = "登录接口和匿名访问接口")
public class VxLoginController {
    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/vxLogin")
    public ResultVO vxLogin(String code){
        log.info("vx-login-code: "+ code);
        //https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=JSCODE&grant_type=authorization_code
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, String> param = new HashMap<>();
        param.put("appid", "wx7e139fc4dec9fd08");
        param.put("secret", "d483c36cf9f93500a17aa0cb788a0f48");
        param.put("js_code", code);
        param.put("grant_type", "authorization_code");

        String vxResult = HttpClientUtil.doGet(url,param);

        VxSessionModel model = JsonUtils.jsonToPojo(vxResult,VxSessionModel.class);

        //将session存入redis
        redisUtil.set("user-redis-session:"+model.getOpenid(),model.getSession_key(),600);
        return ResultVOUtil.success("user-redis-session:"+model.getOpenid());
    }

    @PostMapping("/test")
    public void test(){
        for (int i = 0; i < 10; i++) {
            redisUtil.set("i"+i,i);
        }
    }
}
