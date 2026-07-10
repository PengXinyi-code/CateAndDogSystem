package com.fast.animal.domain;

import lombok.Data;

@Data
public class RecognitionResult {
    private Boolean matched;
    private Boolean catDog;
    private String categoryId;
    private String categoryCode;
    private String categoryName;
    private String breedId;
    private String breedCode;
    private String breedName;
    private Double categoryConfidence;
    private Double breedConfidence;
    private Double confidence;
    private Double similarity;
    private String rawDetectLabel;
    private String rawBreedLabel;
    private String message;
    private Animal animal;
}
