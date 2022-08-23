package com.etian.configuration.authentication;

import com.etian.authentication.service.JwtService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Description: 鉴权属性配置类
 * @ClassName: AuthenticationAutoConfiguration
 * @Author: lisuyi
 * @Date: 2022/1/5 16:04
 * @Version: 1.0
 */
@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
@Import(JwtService.class)
public class AuthenticationAutoConfiguration {
}
