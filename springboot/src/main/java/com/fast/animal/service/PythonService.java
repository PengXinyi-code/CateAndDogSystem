package com.fast.animal.service;

import com.fast.animal.domain.CatDogAnalysisResult;

public interface PythonService {
    float[] extractFeatureByPath(String path) throws Exception;

    CatDogAnalysisResult analyzeCatDogByPath(String path) throws Exception;
}
