package com.fast.succour.domain;

import lombok.Data;

@Data
public class RecognitionResult {
    private Boolean matched;
    private Boolean catDog;
    private String categoryId;
    private String categoryCode;
    private String categoryName;
    private Double confidence;
    private String message;
    private Animal animal;
}
