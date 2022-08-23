package com.etian.authentication.service;

import com.etian.common.JsonUtil;
import com.etian.common.RedisCacheConstant;
import com.etian.entity.userservice.UserEntity;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户权限服务
 *
 * @author wuwei
 * @date 2022/8/8
 **/
@Service
@Slf4j
public class UserPermissionService {

    @Resource(name="redisTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 根据用户Id从缓存获取以及授权的url
     * @param userId 用户Id
     * @param saasId 租户Id
     * @return Set<String>
     */
    public Set<String> findPermissionUrls(String userId, String saasId) {
        log.info("获取用户的授权URL信息，userId:{}, saasId:{}", userId, saasId);
        //1、获取角色信息
        String roleIdsStr = (String) redisTemplate.opsForHash().get(RedisCacheConstant.USER_ROLES_HASH + userId, saasId);
        if (StringUtils.isBlank(roleIdsStr)) {
            log.error("用户未获取到有效的角色信息，userId:{}, saasId:{}", userId, saasId);
            return Sets.newHashSet();
        }
        List<String> roleIds = Arrays.asList(roleIdsStr.split(","));
        //2、根据角色信息获取授权的菜单Id信息
        List<Object> menuList = redisTemplate.executePipelined((RedisCallback<String>) connection -> {
            for (String roleId : roleIds) {
                String key = RedisCacheConstant.ROLE_MENUS_KV + roleId;
                connection.get(key.getBytes());
            }
            return null;
        });

        if (CollectionUtils.isEmpty(menuList)) {
            log.error("根据角色获取关联的菜单Id数据为空，roleIds:{}", JsonUtil.toJson(roleIds));
            return Sets.newHashSet();
        }
        Set<String> menuIds = new HashSet<>(100);
        for (Object menu : menuList) {
            String menuStr = (String) menu;
            if (StringUtils.isBlank(menuStr)) {
                continue;
            }
            menuIds.addAll(Arrays.asList(menuStr.split(",")));
        }

        //3、根据菜单Id批量获取菜单信息
        List<String> menuUrls = redisTemplate.opsForHash().multiGet(RedisCacheConstant.MENU_HASH, menuIds);
        return menuUrls.stream().filter(e->e!=null).map(e->{
            if (e.indexOf("/")>0) {
                return e.substring(e.indexOf("/"));
            }
            return e;
        }).collect(Collectors.toSet());
//        return menuUrls.stream().map(String::valueOf).collect(Collectors.toSet());
    }

    public UserEntity findUserEntity(String userId) {
        String userStr = (String) redisTemplate.opsForValue().get(RedisCacheConstant.LOGIN_USER_KV + userId);
        if (StringUtils.isNotBlank(userStr)) {
           return JsonUtil.fromJsonByType(userStr, UserEntity.class);
        }
        return null;
    }




}
