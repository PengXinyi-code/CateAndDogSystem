package com.fast.succour.service;

import com.fast.succour.domain.Animal;
import com.fast.succour.domain.Category;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AnimalService {

    int updateAnimal(Animal animal) ;

    List<Animal> selectAnimalList(Animal animal);

    Animal findById(Long id);

    int insertAnimal(Animal animal, MultipartFile file, String imageUrl) throws Exception;

    int deleteAnimalByAnimalIds(String[] animalIds);
}
