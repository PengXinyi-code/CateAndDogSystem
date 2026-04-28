package com.fast.succour.mapper;

import com.fast.succour.domain.AnimalImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    int insertAnimalImage(AnimalImage animalImage);

    int updateAnimalImage(AnimalImage animalImage);

    void deleteByAnimalIds(String[] animalIds);
}