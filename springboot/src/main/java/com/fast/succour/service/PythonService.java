package com.fast.succour.service;

import org.springframework.web.multipart.MultipartFile;

public interface PythonService {
//    float[] extractFeature(MultipartFile file) throws Exception;
    public float[] extractFeatureByPath(String path) throws Exception;
}
