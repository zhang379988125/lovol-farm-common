package com.etian.authentication.session;

import com.etian.common.http.Header;
import com.etian.entity.userservice.CapabilityEntity;
import com.etian.entity.userservice.UserEntity;

/**
 * @Description: session管理器
 * @ClassName: AuthSessionManager
 * @Author: lisuyi
 * @Date: 2022/1/6 13:53
 * @Version: 1.0
 */
public class AuthSessionManager {
    private static ThreadLocal<UserEntity> threadLocal = new ThreadLocal<>();

    private static ThreadLocal<CapabilityEntity> capThreadLocal = new ThreadLocal<>();

    private static ThreadLocal<Header> headerThreadLocal = new ThreadLocal<>();

    public static void set(UserEntity session){
        threadLocal.set(session);
    }

    public static void setHeader(Header header){
        headerThreadLocal.set(header);
    }

    public static void setCapability(CapabilityEntity cap){
        capThreadLocal.set(cap);
    }

    public static CapabilityEntity getCapability(){
        return capThreadLocal.get();
    }

    public static Header getHeader(){
        return headerThreadLocal.get();
    }

    public static UserEntity get(){
        return threadLocal.get();
    }

    public static void remove(){
        threadLocal.remove();
        headerThreadLocal.remove();
        capThreadLocal.remove();
    }
}
