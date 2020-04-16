package com.vx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.vx.dao")
public class VxxcxApplication {

    public static void main(String[] args) {
        SpringApplication.run(VxxcxApplication.class, args);
    }

}
