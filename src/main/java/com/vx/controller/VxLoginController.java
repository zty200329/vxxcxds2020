package com.vx.controller;

import com.vx.form.CodeForm;
import com.vx.service.AnonService;
import com.vx.vo.ResultVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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
    private AnonService anonService;
    //注入rabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/vxLogin")
    public ResultVO vxLogin(@Validated CodeForm code, BindingResult bindingResult){
        return anonService.vxLogin(code,bindingResult);
    }

    @PostMapping("/test")
    public void test(){
        CodeForm codeForm = new CodeForm();
        codeForm.setCode("safasdfdsdfasdfasdfa");
        //fanout 广播
            rabbitTemplate.convertAndSend("test","",codeForm);

    }

}
