package com.fast.animal.service.impl;

import com.fast.animal.domain.Breed;
import com.fast.animal.mapper.BreedMapper;
import com.fast.animal.service.IBreedService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BreedServiceImpl implements IBreedService {
    @Resource
    private BreedMapper breedMapper;

    @Override
    public List<Breed> selectBreedList(Breed breed) {
        return breedMapper.selectBreedList(breed);
    }

    @Override
    public Breed selectBreedByBreedId(String breedId) {
        return breedMapper.selectBreedByBreedId(breedId);
    }

    @Override
    public int insertBreed(Breed breed) {
        if (breed.getBreedId() == null || breed.getBreedId().isEmpty()) {
            breed.setBreedId(UUID.randomUUID().toString());
        }
        return breedMapper.insertBreed(breed);
    }

    @Override
    public int updateBreed(Breed breed) {
        return breedMapper.updateBreed(breed);
    }

    @Override
    public int deleteBreedByBreedIds(String[] breedIds) {
        return breedMapper.deleteBreedByBreedIds(breedIds);
    }
}
