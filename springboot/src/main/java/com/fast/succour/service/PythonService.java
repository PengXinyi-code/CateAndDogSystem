package com.fast.succour.service;

import com.fast.succour.domain.CatDogDetectionResult;
import org.springframework.web.multipart.MultipartFile;

public interface PythonService {
//    float[] extractFeature(MultipartFile file) throws Exception;
    public float[] extractFeatureByPath(String path) throws Exception;

    CatDogDetectionResult detectCatDogByPath(String path) throws Exception;
}
