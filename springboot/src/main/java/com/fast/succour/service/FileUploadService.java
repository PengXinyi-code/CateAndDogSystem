package com.fast.succour.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    String saveFileToLocal(MultipartFile file)throws IOException;
}
