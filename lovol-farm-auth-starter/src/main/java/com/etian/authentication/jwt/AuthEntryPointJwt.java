package com.etian.authentication.jwt;

import com.etian.common.ErrCode;
import com.etian.common.http.Header;
import com.etian.common.http.ResponseBean;
import com.etian.entity.userservice.AuthErrCodeBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/* *
 *
 * 鉴权拦截点
 * @author lisuyi
 * @return
 * @date 2022/1/5 17:37
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResponseBean responseBean = new ResponseBean();
        AuthErrCodeBean att = (AuthErrCodeBean) request.getAttribute(AuthTokenFilter.errorMessage);
        response.setStatus(HttpServletResponse.SC_OK);
        if(att == null) {
            att = new AuthErrCodeBean();
        }
        if(att.getCode() == null) {
            att.setCode(ErrCode.ERR_LOGIN_ERROR);
        }
        responseBean.setHeader(Header.getInstance()
                .setErrorMessage(att == null || StringUtils.isEmpty(att.getMessage()) ? "鉴权异常，请联系管理员。" : att.getMessage())
                .setResponseCode(att.getCode())
                .setExtendMessage(att.getExtendMessage())
                .setReferer(request.getServletPath()).setToken(""));
        responseBean.setBody(null);
        logger.error("Unauthorized error: {}", authException, att.getMessage());
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), responseBean);
    }

}
