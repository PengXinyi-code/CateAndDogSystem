package com.fast.succour.domain;

import lombok.Data;

@Data
public class CatDogAnalysisResult {
    private Boolean catDog;
    private String categoryCode;
    private String categoryId;
    private String categoryName;
    private String breedCode;
    private String breedId;
    private String breedName;
    private Double categoryConfidence;
    private Double breedConfidence;
    private String rawDetectLabel;
    private String rawBreedLabel;
    private String cropPath;
    private String message;
}
