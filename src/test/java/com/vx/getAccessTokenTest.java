package com.vx;

import com.vx.service.impl.ActivityServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zty
 * @date 2020/4/23 下午2:34
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class getAccessTokenTest {

    @Test
    public void doTest(){
        ActivityServiceImpl activityService = new ActivityServiceImpl();
        String str = activityService.getAccessToken();
        System.out.println(str);
    }
}
