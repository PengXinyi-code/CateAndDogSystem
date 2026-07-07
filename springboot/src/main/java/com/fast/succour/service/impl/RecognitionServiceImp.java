package com.fast.succour.service.impl;

import com.fast.succour.domain.Animal;
import com.fast.succour.domain.AnimalImage;
import com.fast.succour.domain.CatDogDetectionResult;
import com.fast.succour.domain.RecognitionResult;
import com.fast.succour.mapper.AnimalImageMapper;
import com.fast.succour.mapper.AnimalMapper;
import com.fast.succour.service.PythonService;
import com.fast.succour.service.RecognitionService;
import com.fast.system.general.utils.ProjectPathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
public class RecognitionServiceImp implements RecognitionService {

    @Value("${file.temp-path}")
    private String tempPath;

    @Autowired
    private AnimalImageMapper imageMapper;

    @Autowired
    private AnimalMapper animalMapper;

    @Autowired
    private PythonService pythonService;

    private static final double THRESHOLD = 0.90;

    @Override
    public RecognitionResult recognize(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        RecognitionResult result = new RecognitionResult();
        File dest = saveTempFile(file);

        try {
            CatDogDetectionResult detection = pythonService.detectCatDogByPath(dest.getAbsolutePath());
            fillDetectionResult(result, detection);

            if (!Boolean.TRUE.equals(detection.getCatDog())) {
                result.setMatched(false);
                result.setMessage(defaultMessage(detection.getMessage(), "未检测到猫狗，请上传猫或狗的清晰照片"));
                return result;
            }

            float[] queryFeature = pythonService.extractFeatureByPath(dest.getAbsolutePath());
            List<AnimalImage> images = imageMapper.findByCategoryId(detection.getCategoryId());

            double maxSim = 0;
            Animal bestAnimal = null;

            for (AnimalImage img : images) {
                if (img.getFeatureVector() == null) {
                    continue;
                }

                float[] dbFeature = VectorUtil.bytesToFloatArray(img.getFeatureVector());
                double sim = VectorUtil.cosineSimilarity(queryFeature, dbFeature);

                System.out.println("animal_id=" + img.getAnimalId() + " sim=" + sim);

                if (sim > maxSim) {
                    maxSim = sim;
                    bestAnimal = animalMapper.findById(img.getAnimalId());
                }
            }

            if (maxSim > THRESHOLD) {
                result.setMatched(true);
                result.setAnimal(bestAnimal);
                result.setMessage("识别成功");
                return result;
            }

            result.setMatched(false);
            result.setMessage("未匹配到已建档猫狗，请创建新档案");
            return result;
        } finally {
            deleteTempFile(dest);
        }
    }

    private File saveTempFile(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String suffix = ".jpg";
        if (originalFilename != null && originalFilename.lastIndexOf(".") >= 0) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = "tmp_" + System.currentTimeMillis() + suffix;
        File dest = ProjectPathUtils.resolve(tempPath).resolve(fileName).toFile();
        dest.getParentFile().mkdirs();
        file.transferTo(dest);
        return dest;
    }

    private void fillDetectionResult(RecognitionResult result, CatDogDetectionResult detection) {
        result.setCatDog(detection.getCatDog());
        result.setCategoryId(detection.getCategoryId());
        result.setCategoryCode(detection.getCategoryCode());
        result.setCategoryName(detection.getCategoryName());
        result.setConfidence(detection.getConfidence());
    }

    private String defaultMessage(String message, String fallback) {
        return message == null || message.isEmpty() ? fallback : message;
    }

    private void deleteTempFile(File dest) {
        if (dest != null && dest.exists()) {
            dest.delete();
        }
    }
}
