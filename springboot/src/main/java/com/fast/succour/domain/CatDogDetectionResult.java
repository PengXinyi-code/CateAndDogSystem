package com.fast.succour.domain;

import lombok.Data;

@Data
public class CatDogDetectionResult {
    private Boolean catDog;
    private String categoryCode;
    private String categoryId;
    private String categoryName;
    private Double confidence;
    private String message;
}
