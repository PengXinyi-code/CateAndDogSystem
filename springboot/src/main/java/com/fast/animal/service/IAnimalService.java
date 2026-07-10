package com.fast.animal.service;

import java.util.List;
import com.fast.animal.domain.Animal;

/**
 * 宠物Service接口
 *
 * @author huacai
 * @date 2025-12-16
 */
public interface IAnimalService
{
    /**
     * 查询宠物
     *
     * @param animalId 宠物主键
     * @return 宠物
     */
    public Animal selectAnimalByAnimalId(Long animalId);

    /**
     * 查询宠物列表
     *
     * @param animal 宠物
     * @return 宠物集合
     */
    public List<Animal> selectAnimalList(Animal animal);

    /**
     * 新增宠物
     *
     * @param animal 宠物
     * @return 结果
     */
    public int insertAnimal(Animal animal);

    /**
     * 修改宠物
     *
     * @param animal 宠物
     * @return 结果
     */
    public int updateAnimal(Animal animal);

    /**
     * 批量删除宠物
     *
     * @param animalIds 需要删除的宠物主键集合
     * @return 结果
     */
    public int deleteAnimalByAnimalIds(String[] animalIds);

}
