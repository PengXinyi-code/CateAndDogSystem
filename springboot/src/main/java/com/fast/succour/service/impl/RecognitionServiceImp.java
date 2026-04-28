package com.fast.succour.service.impl;

import com.fast.succour.domain.Animal;
import com.fast.succour.domain.AnimalImage;
import com.fast.succour.mapper.AnimalImageMapper;
import com.fast.succour.mapper.AnimalMapper;
import com.fast.succour.service.PythonService;
import com.fast.succour.service.RecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
public class RecognitionServiceImp implements RecognitionService {

    // 1. 依赖注入应该放在类级别
    @Autowired
    private AnimalImageMapper imageMapper;

    @Autowired
    private AnimalMapper animalMapper;

    @Autowired
    private PythonService pythonService;

    // 2. 常量定义在类级别
    private static final double THRESHOLD = 0.85;

    @Override
    public Animal recognize(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        //先保存到临时文件
        String suffix = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf("."));

        String fileName = "tmp_" + System.currentTimeMillis() + suffix;

        String tempDir = "E:/animal-images/temp/"; // 单独临时目录（推荐）
        File dest = new File(tempDir + fileName);
        dest.getParentFile().mkdirs();

        file.transferTo(dest);

        //用路径提特征
        float[] queryFeature = pythonService.extractFeatureByPath(dest.getAbsolutePath());

        // 取数据库向量
        List<AnimalImage> images = imageMapper.findAll();

        double maxSim = 0;
        Animal bestAnimal = null;

        for (AnimalImage img : images) {

            if (img.getFeatureVector() == null) continue;

            float[] dbFeature = VectorUtil.bytesToFloatArray(img.getFeatureVector());

            if (dbFeature == null || queryFeature == null) continue;

            double sim = VectorUtil.cosineSimilarity(queryFeature, dbFeature);

            System.out.println("animal_id=" + img.getAnimalId() + " sim=" + sim);

            if (sim > maxSim) {
                maxSim = sim;
                bestAnimal = animalMapper.findById(img.getAnimalId());
            }
        }

        //删除临时文件
        if (dest.exists()) {
            dest.delete();
        }

        // 阈值判断（关键）
        if (maxSim > 0.85) {
            return bestAnimal;
        }

        return null;
    }
}