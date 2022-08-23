package com.etian.authentication.service;

import com.etian.authentication.bean.UserDetailsImpl;
import com.etian.entity.userservice.UserEntity;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private ThreadLocal<String> token = new ThreadLocal<>();

    public void setToken(String token) {
        this.token.set(token);
    }

    @Override
    public UserDetailsImpl loadUserByUsername(String usernameAndInstitutionId) throws UsernameNotFoundException {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority((usernameAndInstitutionId)));
        /**
         * TODO 查询数据库
         * @author zhanghuanliang
         * @date 2022/4/25 0025 15:16
         */
        return new UserDetailsImpl(null, authorities);
    }

    public UserDetailsImpl loadDefaultUser(UserEntity user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority((user.getUsername())));
        return new UserDetailsImpl(user, authorities);
    }

}
