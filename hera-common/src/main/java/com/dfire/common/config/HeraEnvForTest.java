package com.dfire.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author lenovo
 * @description
 * @date 2020/7/14 10:08
 */
@Component
public class HeraEnvForTest {
    private String envFlag;

    public String getEnvFlag() {
        return envFlag;
    }

    @Value("${hera.envFlag}")
    public void setEnvFlag(String envFlag) {
        this.envFlag = envFlag;
    }


}
