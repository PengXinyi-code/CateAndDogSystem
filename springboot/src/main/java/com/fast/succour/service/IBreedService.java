package com.fast.succour.service;

import com.fast.succour.domain.Breed;

import java.util.List;

public interface IBreedService {
    List<Breed> selectBreedList(Breed breed);

    Breed selectBreedByBreedId(String breedId);

    int insertBreed(Breed breed);

    int updateBreed(Breed breed);

    int deleteBreedByBreedIds(String[] breedIds);
}
