package com.etian.authentication.interceptor;

import com.etian.authentication.util.AuthConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * 内部接口fegin调用添加header
 *
 * @author zhangguofeng
 */
public class InnerFeginInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header(AuthConstant.INNER_INVOKE, "true");
    }
}
