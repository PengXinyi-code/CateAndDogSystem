package com.fast.animal.mapper;

import com.fast.animal.domain.Animal;
import com.fast.animal.domain.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnimalMapper {

    List<Animal> selectAnimalList(Animal animal);

    Map<String, Object> selectAnimalStats();

    Animal findById(Long id);

    int insertAnimal(Animal animal);

    int updateAnimal(Animal animal);

    int deleteAnimalByAnimalIds(String[] animalIds);

}
