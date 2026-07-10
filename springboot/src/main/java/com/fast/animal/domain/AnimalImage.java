package com.fast.animal.domain;

import lombok.Data;

@Data
public class AnimalImage {
    private Long id;
    private Long animalId;
    private String imageUrl;
    private byte[] featureVector;
}