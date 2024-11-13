package com.dfire.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:09 2018/5/22
 * @desc
 */
@Configuration
public class WebSecurityConfig
        extends WebMvcConfigurerAdapter
{
    public final static String SESSION_USERNAME = "username";
    public final static String SESSION_USER_ID = "userId";
    public final static String TOKEN_NAME = "accessToken";

    @Bean
    public SecurityInterceptor getSecurityInterceptor()
    {
        return new SecurityInterceptor();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        registry.addMapping("/**").allowedOrigins();
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry)
    {
        InterceptorRegistration addRegistry = interceptorRegistry.addInterceptor(getSecurityInterceptor());
        addRegistry
                .excludePathPatterns("/error")
                .excludePathPatterns("/login**")
                .excludePathPatterns("/auth/**");
    }
}
