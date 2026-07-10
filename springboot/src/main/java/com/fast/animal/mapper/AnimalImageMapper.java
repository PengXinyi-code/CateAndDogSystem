package com.fast.animal.mapper;

import com.fast.animal.domain.AnimalImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnimalImageMapper {

//    @Select("""
//    SELECT
//        id,
//        animal_id AS animalId,
//        image_url AS imageUrl,
//        feature_vector AS featureVector
//    FROM animal_images
//    """)
    List<AnimalImage> findAll();

    List<AnimalImage> findByCategoryId(String categoryId);

    List<AnimalImage> findByCategoryIdAndBreedId(@Param("categoryId") String categoryId, @Param("breedId") String breedId);

    int insertAnimalImage(AnimalImage animalImage);

    int updateAnimalImage(AnimalImage animalImage);

    void deleteByAnimalIds(String[] animalIds);
}
