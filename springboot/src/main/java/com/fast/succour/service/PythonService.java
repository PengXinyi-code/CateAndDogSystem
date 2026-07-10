package com.fast.succour.service;

import com.fast.succour.domain.CatDogAnalysisResult;

public interface PythonService {
    float[] extractFeatureByPath(String path) throws Exception;

    CatDogAnalysisResult analyzeCatDogByPath(String path) throws Exception;
}
