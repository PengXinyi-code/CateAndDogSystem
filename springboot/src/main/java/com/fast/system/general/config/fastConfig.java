package com.fast.system.general.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 *
 * @author fast
 */
@Component
@ConfigurationProperties(prefix = "fast")
public class fastConfig {
    /**
     * 上传路径
     */
    private static String profile;

    /**
     * 验证码类型
     */
    private static String captchaType;

    public static String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        fastConfig.profile = profile;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        fastConfig.captchaType = captchaType;
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }
}
