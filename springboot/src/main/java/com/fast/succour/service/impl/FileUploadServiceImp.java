package com.fast.succour.service.impl;

import com.fast.succour.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileUploadServiceImp implements FileUploadService {

    // 1. 从配置文件中读取 upload.path 属性的值
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public String saveFileToLocal(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        // 2. 生成文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) return null;

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = java.util.UUID.randomUUID().toString().replace("-", "") + suffix;

        // 3. 保存文件
        // 直接使用从配置文件注入的 uploadPath
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, newFileName);
        System.out.println("文件保存路径: " + dest.getAbsolutePath());
        file.transferTo(dest);

        // 4. 返回访问路径
        return "/uploads/images/" + newFileName;
    }
}