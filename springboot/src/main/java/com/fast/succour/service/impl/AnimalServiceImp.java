package com.fast.succour.service.impl;

import com.fast.succour.domain.Animal;
import com.fast.succour.domain.AnimalImage;
import com.fast.succour.mapper.AnimalImageMapper;
import com.fast.succour.mapper.AnimalMapper;
import com.fast.succour.service.AnimalService;
import com.fast.succour.service.FileUploadService;
import com.fast.succour.service.PythonService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

@Service
public class AnimalServiceImp implements AnimalService {
    @Resource
    private AnimalMapper animalMapper;

    @Resource
    private AnimalImageMapper animalImageMapper;//注入图片

    @Autowired
    private FileUploadService fileUploadService; // 注入文件服务

    @Autowired
    PythonService pythonService;

    @Override
    public List<Animal> selectAnimalList(Animal animal) {
        return animalMapper.selectAnimalList(animal);
    }

    @Override
    public Animal findById(Long id) {
        return animalMapper.findById(id);
    }

    @Override
    @Transactional
    public int insertAnimal(Animal animal, MultipartFile file, String imageUrl) throws Exception {

        String savedImageUrl = null;
        float[] feature = null;

        if (file != null && !file.isEmpty()) {
            // 唯一文件名
            String original = file.getOriginalFilename();
            String suffix = original.substring(original.lastIndexOf("."));
            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + suffix;

            // 固定磁盘目录
            String uploadDir = "file:C:/Users/詹雨莹/Desktop/Cat-and-Dog-System/springboot/src/main/resources/static/uploads/images/";
            File dest = new File(uploadDir + fileName);
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
            String fullPath = "E:/Cat-and-Dog-System/springboot/src/main/resources/static" + imageUrl;
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
                    String uploadDir = "D:/Desktop/CateAndDogSystem/springboot/src/main/resources/static/uploads/images";
                    String fullPath = uploadDir + fileName;
                    File fileToDelete = new File(fullPath);
                    
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
}
