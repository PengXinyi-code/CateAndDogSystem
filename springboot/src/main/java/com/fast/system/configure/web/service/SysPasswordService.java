package com.fast.system.configure.web.service;

import com.fast.system.domain.SysUser;
import com.fast.system.configure.security.context.AuthenticationContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 登录密码方法
 *
 * @author fast
 */
@Component
public class SysPasswordService
{
    public void validate(SysUser user)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        if (!matches(user, password))
        {
            throw new RuntimeException("用户不存在/密码错误");
        }
    }

    public boolean matches(SysUser user, String rawPassword)
    {
        return user.getPassword().equals(rawPassword);
    }
}
