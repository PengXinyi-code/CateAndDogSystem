package com.fast.succour.controller;

import com.fast.succour.service.FileUploadService;
import com.fast.system.general.core.controller.BaseController;
import com.fast.system.general.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/common")
public class UploadController extends BaseController {

    @Autowired
    private FileUploadService fileUploadService; // 注入文件服务

    // 这个接口只负责接收请求，然后转交给 Service
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadService.saveFileToLocal(file);
            return success("上传成功").put("url", url);
        } catch (Exception e) {
            return error("上传失败：" + e.getMessage());
        }
    }
}
