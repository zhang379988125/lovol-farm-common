package com.etian.authentication.service;

import com.etian.authentication.bean.TokenUser;
import com.etian.common.ErrCode;
import com.etian.common.Pair;
import com.etian.configuration.authentication.AuthenticationProperties;
import com.etian.exception.CustomException;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sound.midi.Soundbank;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    private AuthenticationProperties authenticationProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String PREFIX_HASH_USER_TOKEN = "user:token"; // <前缀:>

    private static final long EXPIRE_TIME_HOUR = 60 * 60 * 8;//默认过期时间八小时


    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + authenticationProperties.getJwtExpirationMsg()))
                .signWith(SignatureAlgorithm.HS512, authenticationProperties.getJwtSecret())
                .compact();
    }

    public String generateJwtTokenByUserName(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + authenticationProperties.getJwtExpirationMsg()))
                .signWith(SignatureAlgorithm.HS512, authenticationProperties.getJwtSecret())
                .compact();
    }

    public TokenUser getUserNameFromJwtToken(String token) {
        TokenUser tokenUser = null;
        String subject = Jwts.parser().setSigningKey(authenticationProperties.getJwtSecret()).parseClaimsJws(token).getBody().getSubject();
        if(StringUtils.isNotEmpty(subject)) {
            String[] arrays = subject.split(":");
            tokenUser = new TokenUser();
            tokenUser.setUsername(arrays[0]);
            tokenUser.setInstitutionId(arrays[1]);
            if (arrays.length>2) {
                tokenUser.setUserId(arrays[2]);
                tokenUser.setSaasId(arrays[3]);
            }
        }
        return tokenUser;
    }

    public boolean validateJwtToken(String authToken) throws CustomException {
        try {
            Jwts.parser().setSigningKey(authenticationProperties.getJwtSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e);
            throw new CustomException(ErrCode.ERR_LOGIN_ERROR, "token格式错误，请重新登录");
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e);
            throw new CustomException(ErrCode.ERR_LOGIN_ERROR, "token格式错误，请重新登录");
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e);
            throw new CustomException(ErrCode.ERR_LOGIN_ERROR, "token过期，请重新登录");
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e);
            throw new CustomException(ErrCode.ERR_LOGIN_ERROR, "token格式错误，请重新登录。");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e);
            throw new CustomException(ErrCode.ERR_LOGIN_ERROR, "token声明格式错误，请重新登录");
        } catch (Exception ex) {
            logger.error("Invalid JWT token: {}", ex);
            throw new CustomException(ErrCode.ERR_LOGIN_ERROR, "token格式错误，请重新登录");
        }
    }

    public String validateExpiration(String token) {
        String tokenRedis = (String) redisTemplate.opsForValue().get(PREFIX_HASH_USER_TOKEN + ":" + token);
        if (StringUtils.isNotEmpty(tokenRedis)){
            return tokenRedis;
        }
        Claims claim = Jwts.parser().setSigningKey(authenticationProperties.getJwtSecret()).parseClaimsJws(token).getBody();
        Date expiration = claim.getExpiration();
        long interval = expiration.getTime() - System.currentTimeMillis();
        float ratio=(float) interval/authenticationProperties.getJwtExpirationMsg();
        if (ratio<=0.2){
            String refreshToken = generateJwtToken(claim.getSubject());
            redisTemplate.opsForValue().set(PREFIX_HASH_USER_TOKEN + ":" + token,refreshToken,EXPIRE_TIME_HOUR, TimeUnit.SECONDS);
            return refreshToken;
        }
        return null;
    }

}
