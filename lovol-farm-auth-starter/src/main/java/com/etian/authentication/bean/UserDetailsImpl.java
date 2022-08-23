package com.etian.authentication.bean;

import com.etian.entity.userservice.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private UserEntity authUserEntity;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(UserEntity authUserEntity,
                           Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
        this.authUserEntity = authUserEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.authUserEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return this.authUserEntity.getUsername();
    }

    public UserEntity getAuthUserEntity() {
        return authUserEntity;
    }

    public void setAuthUserEntity(UserEntity authUserEntity) {
        this.authUserEntity = authUserEntity;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(getUsername(), getUsername());
    }
}
