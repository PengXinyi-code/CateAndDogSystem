package com.fast.animal.mapper;

import com.fast.animal.domain.Breed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BreedMapper {
    List<Breed> selectBreedList(Breed breed);

    Breed selectBreedByBreedId(String breedId);

    Breed selectDefaultBreedByCategoryId(String categoryId);

    Breed selectBreedByCategoryIdAndCode(@Param("categoryId") String categoryId, @Param("code") String code);

    int insertBreed(Breed breed);

    int updateBreed(Breed breed);

    int deleteBreedByBreedIds(String[] breedIds);
}
