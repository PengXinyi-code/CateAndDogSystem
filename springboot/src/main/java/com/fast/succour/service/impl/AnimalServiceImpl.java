package com.fast.succour.service.impl;

import com.fast.succour.domain.Animal;
import com.fast.succour.mapper.AnimalMapper;
import com.fast.succour.service.IAnimalService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalServiceImpl implements IAnimalService {

    @Resource
    private AnimalMapper animalMapper;

    /**
     * 根据ID查询宠物
     */
    @Override
    public Animal selectAnimalByAnimalId(Long animalId) {
        // 调用 Mapper 的 findById 方法
        return animalMapper.findById(animalId);
    }

    /**
     * 查询宠物列表
     */
    @Override
    public List<Animal> selectAnimalList(Animal animal) {
        // 调用 Mapper 的 selectAnimalList 方法
        return animalMapper.selectAnimalList(animal);
    }

    /**
     * 新增宠物
     */
    @Override
    public int insertAnimal(Animal animal) {
        return animalMapper.insertAnimal(animal);
    }

    /**
     * 修改宠物
     */
    @Override
    public int updateAnimal(Animal animal) {
        return animalMapper.updateAnimal(animal);
    }

    /**
     * 批量删除宠物
     */
    @Override
    public int deleteAnimalByAnimalIds(String[] animalIds) {
        return animalMapper.deleteAnimalByAnimalIds(animalIds);
    }
}