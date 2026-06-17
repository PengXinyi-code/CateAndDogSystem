package com.fast.system.general.utils.file;

import com.fast.system.general.config.fastConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LocalUploadFileUtils {
    private static final Logger log = LoggerFactory.getLogger(LocalUploadFileUtils.class);
    private static final String PROFILE_PREFIX = "/profile/";

    private LocalUploadFileUtils() {
    }

    public static boolean deleteProfileFile(String resourceUrl) {
        String resourcePath = extractResourcePath(resourceUrl);
        if (resourcePath == null
                || (!resourcePath.startsWith("/profile/avatar/")
                && !resourcePath.startsWith("/profile/upload/"))) {
            return false;
        }

        try {
            Path profileRoot = Paths.get(fastConfig.getProfile()).toAbsolutePath().normalize();
            Path target = profileRoot
                    .resolve(resourcePath.substring(PROFILE_PREFIX.length()))
                    .normalize();

            if (!target.startsWith(profileRoot)) {
                log.warn("拒绝删除上传目录外的文件: {}", resourceUrl);
                return false;
            }
            return Files.deleteIfExists(target);
        } catch (Exception e) {
            log.warn("删除上传文件失败: {}", resourceUrl, e);
            return false;
        }
    }

    private static String extractResourcePath(String resourceUrl) {
        if (resourceUrl == null || resourceUrl.isBlank()) {
            return null;
        }

        String value = resourceUrl.trim().replace('\\', '/');
        if (value.startsWith("http://") || value.startsWith("https://")) {
            try {
                value = URI.create(value).getPath();
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return value;
    }
}
