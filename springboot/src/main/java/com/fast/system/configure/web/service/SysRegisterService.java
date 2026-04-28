package com.fast.system.configure.web.service;

import com.fast.system.domain.SysUser;
import com.fast.system.general.constant.UserConstants;
import com.fast.system.general.core.domain.model.RegisterBody;
import com.fast.system.general.utils.StringUtils;
import com.fast.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 注册校验方法
 *
 * @author fast
 */
@Component
public class SysRegisterService {
    @Resource
    private ISysUserService userService;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        if (StringUtils.isEmpty(username)) {
            msg = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            msg = "用户密码不能为空";
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = "账户长度必须在2到20个字符之间";
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = "密码长度必须在5到20个字符之间";
        } else {
            sysUser.setNickName(username);
            sysUser.setPassword(password);
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag) {
                msg = "注册失败,请联系系统管理人员";
            }
        }
        return msg;
    }
}
