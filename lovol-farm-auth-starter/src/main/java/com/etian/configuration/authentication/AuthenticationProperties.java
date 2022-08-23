package com.etian.configuration.authentication;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @Description: 属性配置类
 * @ClassName: AuthenticationProperties
 * @Author: lisuyi
 * @Date: 2022/1/5 15:54
 * @Version: 1.0
 */
@Component
@ConfigurationProperties(prefix = "etian.auth")
@Data
@Primary
@ComponentScan({"com.etian.authentication"})
public class AuthenticationProperties {
    private String jwtSecret;
    private long jwtExpirationMsg;
}
