package com.fast.system.controller;

import com.fast.system.general.config.fastConfig;
import com.fast.system.general.core.controller.BaseController;
import com.fast.system.general.core.domain.AjaxResult;
import com.fast.system.domain.SysUser;
import com.fast.system.general.core.domain.model.LoginUser;
import com.fast.system.general.utils.file.FileUploadUtils;
import com.fast.system.general.utils.file.MimeTypeUtils;
import com.fast.system.service.ISysUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 个人信息 业务处理
 *
 * @author fast
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController {
    @Resource
    private ISysUserService userService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile() {
        LoginUser loginUser = getLoginUser();
        SysUser user = loginUser.getUser();
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
        return ajax;
    }

    /**
     * 修改用户
     */
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user) {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getUser();
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (!user.getPhonenumber().isEmpty() && !userService.checkPhoneUnique(currentUser)) {
            return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
        }
        if (!user.getEmail().isEmpty() && !userService.checkEmailUnique(currentUser)) {
            return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginUser loginUser = getLoginUser();
        String userName = loginUser.getUsername();

        // 从数据库获取用户信息，确保密码字段正确
        SysUser user = userService.selectUserByUserName(userName);
        String password = user.getPassword();

        if (!oldPassword.equals(password)) {
            return error("修改密码失败，旧密码错误");
        }
        if (newPassword.equals(password)) {
            return error("新密码不能与旧密码相同");
        }

        if (userService.resetUserPwd(userName, newPassword) > 0) {
            // 更新缓存用户密码
            loginUser.getUser().setPassword(newPassword);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            LoginUser loginUser = getLoginUser();
            String avatar = FileUploadUtils.upload(fastConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
            if (userService.updateUserAvatar(loginUser.getUsername(), avatar)) {
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                // 更新缓存用户头像
                loginUser.getUser().setAvatar(avatar);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
