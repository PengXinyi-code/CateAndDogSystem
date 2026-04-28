package com.fast.succour.mapper;

import com.fast.succour.domain.Animal;
import com.fast.succour.domain.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnimalMapper {

    List<Animal> selectAnimalList(Animal animal);

    Animal findById(Long id);

    int insertAnimal(Animal animal);

    int updateAnimal(Animal animal);

    int deleteAnimalByAnimalIds(String[] animalIds);

}