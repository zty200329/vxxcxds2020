package com.vx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author zty
 * @email i@merryyou.cn
 * @since 1.0
 */
@Configuration
public class WebAppConfigurer extends WebMvcConfigurerAdapter {

    /**
     * 在配置文件中配置的文件保存路径
     */
    @Value("${image.location}")
    private String folder;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**").addResourceLocations("file:" + folder);
        super.addResourceHandlers(registry);
    }

}
