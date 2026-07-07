package com.fast.succour.service.impl;

import com.fast.succour.domain.Animal;
import com.fast.succour.domain.AnimalImage;
import com.fast.succour.domain.Breed;
import com.fast.succour.domain.Category;
import com.fast.succour.mapper.AnimalImageMapper;
import com.fast.succour.mapper.AnimalMapper;
import com.fast.succour.mapper.BreedMapper;
import com.fast.succour.mapper.CategoryMapper;
import com.fast.succour.service.AnimalService;
import com.fast.succour.service.FileUploadService;
import com.fast.succour.service.PythonService;
import com.fast.system.general.utils.ProjectPathUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AnimalServiceImp implements AnimalService {
    @Value("${file.image-path}")
    private String imagePath;

    @Value("${file.static-path}")
    private String staticPath;

    @Resource
    private AnimalMapper animalMapper;

    @Resource
    private AnimalImageMapper animalImageMapper;//注入图片

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private BreedMapper breedMapper;

    @Autowired
    private FileUploadService fileUploadService; // 注入文件服务

    @Autowired
    PythonService pythonService;

    @Override
    public List<Animal> selectAnimalList(Animal animal) {
        return animalMapper.selectAnimalList(animal);
    }

    @Override
    public Map<String, Object> selectAnimalStats() {
        return animalMapper.selectAnimalStats();
    }

    @Override
    public Animal findById(Long id) {
        return animalMapper.findById(id);
    }

    @Override
    @Transactional
    public int insertAnimal(Animal animal, MultipartFile file, String imageUrl) throws Exception {
        normalizeCategoryAndBreed(animal, true);

        String savedImageUrl = null;
        float[] feature = null;

        if (file != null && !file.isEmpty()) {
            // 唯一文件名
            String original = file.getOriginalFilename();
            String suffix = original.substring(original.lastIndexOf("."));
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + suffix;

            // 固定磁盘目录
            File dest = ProjectPathUtils.resolve(imagePath).resolve(fileName).toFile();
            dest.getParentFile().mkdirs();

            // 保存文件
            file.transferTo(dest);

            System.out.println("图片保存路径: " + dest.getAbsolutePath());

            // 用路径提特征
            feature = pythonService.extractFeatureByPath(dest.getAbsolutePath());

            savedImageUrl = "/uploads/images/" + fileName;
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            // 如果前端传了imageUrl（已上传的图片路径），直接使用
            savedImageUrl = imageUrl;

            // 从已保存的文件中提取特征
            String fullPath = ProjectPathUtils.resolve(staticPath).resolve(imageUrl.replaceFirst("^/", "")).toString();
            File existingFile = new File(fullPath);

            System.out.println("尝试从路径提取特征: " + fullPath);
            System.out.println("文件是否存在: " + existingFile.exists());

            if (existingFile.exists()) {
                try {
                    feature = pythonService.extractFeatureByPath(existingFile.getAbsolutePath());
                    System.out.println("特征提取成功，特征维度: " + (feature != null ? feature.length : "null"));
                } catch (Exception e) {
                    System.err.println("特征提取失败: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("错误：找不到图片文件: " + fullPath);
            }
        }

        // 如果特征向量仍为null，使用默认零向量作为兜底
        if (feature == null) {
            System.out.println("警告：特征向量为null，使用默认零向量（512维）");
            feature = new float[512];
        }

        // 设置图片URL
        animal.setImageUrl(savedImageUrl);

        // 存 animals 表
        int rows = animalMapper.insertAnimal(animal);

        // 如果有图片，存 animal_images 表
        if (rows > 0 && savedImageUrl != null) {
            AnimalImage img = new AnimalImage();
            img.setAnimalId(animal.getId());
            img.setImageUrl(savedImageUrl);

            if (feature != null) {
                ByteBuffer buffer = ByteBuffer.allocate(feature.length * 4);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                for (float f : feature) {
                    buffer.putFloat(f);
                }

                img.setFeatureVector(buffer.array());
            }

            animalImageMapper.insertAnimalImage(img);
        }

        return rows;
    }

    @Override
    @Transactional
    public int updateAnimal(Animal animal) {
        normalizeCategoryAndBreed(animal, false);
        int rows = animalMapper.updateAnimal(animal);

        if (rows > 0 && animal.getImageUrl() != null) {
            AnimalImage animalImage = new AnimalImage();
            animalImage.setAnimalId(animal.getId());
            animalImage.setImageUrl(animal.getImageUrl());
            animalImageMapper.updateAnimalImage(animalImage);
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteAnimalByAnimalIds(String[] animalIds) {
        // 先获取要删除的动物的图片路径
        for (String animalId : animalIds) {
            try {
                Long id = Long.parseLong(animalId);
                Animal animal = animalMapper.findById(id);
                if (animal != null && animal.getImageUrl() != null) {
                    // 构建物理文件路径
                    String imageUrl = animal.getImageUrl();
                    System.out.println("准备删除动物ID: " + id + ", 图片URL: " + imageUrl);
                    
                    // 去掉 /uploads/images/ 前缀，获取文件名
                    String fileName = imageUrl.replace("/uploads/images/", "");
                    File fileToDelete = ProjectPathUtils.resolve(imagePath).resolve(fileName).toFile();
                    String fullPath = fileToDelete.getAbsolutePath();
                    
                    System.out.println("尝试删除文件: " + fullPath);
                    System.out.println("文件是否存在: " + fileToDelete.exists());
                    
                    // 删除物理文件
                    if (fileToDelete.exists()) {
                        boolean deleted = fileToDelete.delete();
                        if (deleted) {
                            System.out.println("成功删除图片文件: " + fileToDelete.getAbsolutePath());
                        } else {
                            System.out.println("删除图片文件失败: " + fileToDelete.getAbsolutePath());
                        }
                    } else {
                        System.out.println("文件不存在，跳过删除: " + fullPath);
                    }
                } else {
                    System.out.println("动物ID: " + animalId + " 没有图片URL或动物不存在");
                }
            } catch (NumberFormatException e) {
                System.err.println("无效的动物ID: " + animalId);
            }
        }
        
        // 先删子表图片，再删主表动物
        animalImageMapper.deleteByAnimalIds(animalIds);
        return animalMapper.deleteAnimalByAnimalIds(animalIds);
    }

    private void normalizeCategoryAndBreed(Animal animal, boolean required) {
        if (animal == null) {
            return;
        }

        boolean hasCategoryInput = hasText(animal.getCategoryId()) || hasText(animal.getSpecies());
        boolean hasBreedInput = hasText(animal.getBreedId());

        if (!required && !hasCategoryInput && !hasBreedInput) {
            return;
        }

        Category category = null;
        if (hasText(animal.getCategoryId())) {
            category = categoryMapper.selectCategoryByCategoryId(animal.getCategoryId());
        } else if ("猫".equals(animal.getSpecies())) {
            category = categoryMapper.selectCategoryByCode("cat");
        } else if ("狗".equals(animal.getSpecies())) {
            category = categoryMapper.selectCategoryByCode("dog");
        }

        if (category == null || Boolean.FALSE.equals(category.getEnabled())) {
            if (required) {
                throw new RuntimeException("请选择有效的动物类别");
            }
            if (hasCategoryInput) {
                throw new RuntimeException("动物类别无效或已停用");
            }
            return;
        }

        animal.setCategoryId(category.getCategoryId());
        animal.setSpecies(category.getName());

        Breed breed = null;
        if (hasText(animal.getBreedId())) {
            breed = breedMapper.selectBreedByBreedId(animal.getBreedId());
            if (breed == null || Boolean.FALSE.equals(breed.getEnabled())) {
                throw new RuntimeException("动物品种无效或已停用");
            }
            if (!category.getCategoryId().equals(breed.getCategoryId())) {
                throw new RuntimeException("动物品种与动物类别不匹配");
            }
        } else {
            breed = breedMapper.selectDefaultBreedByCategoryId(category.getCategoryId());
        }

        if (breed != null) {
            animal.setBreedId(breed.getBreedId());
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
