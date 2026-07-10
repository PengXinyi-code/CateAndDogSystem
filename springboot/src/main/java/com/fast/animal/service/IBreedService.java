package com.fast.animal.service;

import com.fast.animal.domain.Breed;

import java.util.List;

public interface IBreedService {
    List<Breed> selectBreedList(Breed breed);

    Breed selectBreedByBreedId(String breedId);

    int insertBreed(Breed breed);

    int updateBreed(Breed breed);

    int deleteBreedByBreedIds(String[] breedIds);
}
