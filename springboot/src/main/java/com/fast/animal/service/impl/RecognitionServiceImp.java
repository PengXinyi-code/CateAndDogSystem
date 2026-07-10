package com.fast.animal.service.impl;

import com.fast.animal.domain.Animal;
import com.fast.animal.domain.AnimalImage;
import com.fast.animal.domain.Breed;
import com.fast.animal.domain.CatDogAnalysisResult;
import com.fast.animal.domain.Category;
import com.fast.animal.domain.RecognitionResult;
import com.fast.animal.mapper.AnimalImageMapper;
import com.fast.animal.mapper.AnimalMapper;
import com.fast.animal.mapper.BreedMapper;
import com.fast.animal.mapper.CategoryMapper;
import com.fast.animal.service.PythonService;
import com.fast.animal.service.RecognitionService;
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
    private CategoryMapper categoryMapper;

    @Autowired
    private BreedMapper breedMapper;

    @Autowired
    private PythonService pythonService;

    @Value("${recognition.match-threshold:0.90}")
    private double matchThreshold;

    @Override
    public RecognitionResult recognize(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        RecognitionResult result = new RecognitionResult();
        File dest = saveTempFile(file);
        File cropFile = null;

        try {
            CatDogAnalysisResult analysis = pythonService.analyzeCatDogByPath(dest.getAbsolutePath());
            fillAnalysisResult(result, analysis);

            if (!Boolean.TRUE.equals(analysis.getCatDog())) {
                result.setMatched(false);
                result.setMessage(defaultMessage(analysis.getMessage(), "未检测到猫狗，请上传猫或狗的清晰照片"));
                return result;
            }

            Category category = resolveCategory(analysis);
            Breed breed = resolveBreed(category.getCategoryId(), analysis.getBreedCode());
            fillDictionaryResult(result, category, breed);
            System.out.println("[recognition] categoryCode=" + analysis.getCategoryCode()
                    + " categoryConfidence=" + analysis.getCategoryConfidence()
                    + " rawBreedLabel=" + analysis.getRawBreedLabel()
                    + " breedCode=" + analysis.getBreedCode()
                    + " breedConfidence=" + analysis.getBreedConfidence()
                    + " resolvedBreedId=" + breed.getBreedId()
                    + " resolvedBreedName=" + breed.getName());

            String featurePath = hasText(analysis.getCropPath()) ? analysis.getCropPath() : dest.getAbsolutePath();
            if (hasText(analysis.getCropPath())) {
                cropFile = new File(analysis.getCropPath());
            }
            float[] queryFeature = pythonService.extractFeatureByPath(featurePath);
            List<AnimalImage> images = imageMapper.findByCategoryIdAndBreedId(category.getCategoryId(), breed.getBreedId());

            double maxSim = 0;
            Animal bestAnimal = null;

            for (AnimalImage img : images) {
                if (img.getFeatureVector() == null) {
                    continue;
                }

                float[] dbFeature = VectorUtil.bytesToFloatArray(img.getFeatureVector());
                double sim = VectorUtil.cosineSimilarity(queryFeature, dbFeature);

                // 逐条相似度日志输出量较大，当前优先观察猫狗检测框和裁剪质量，必要时再临时打开。
                // System.out.println("animal_id=" + img.getAnimalId() + " sim=" + sim);

                if (sim > maxSim) {
                    maxSim = sim;
                    bestAnimal = animalMapper.findById(img.getAnimalId());
                }
            }

            result.setSimilarity(maxSim);
            if (maxSim > matchThreshold) {
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
            deleteTempFile(cropFile);
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

    private void fillAnalysisResult(RecognitionResult result, CatDogAnalysisResult analysis) {
        result.setCatDog(analysis.getCatDog());
        result.setCategoryId(analysis.getCategoryId());
        result.setCategoryCode(analysis.getCategoryCode());
        result.setCategoryName(analysis.getCategoryName());
        result.setBreedCode(analysis.getBreedCode());
        result.setBreedName(analysis.getBreedName());
        result.setCategoryConfidence(analysis.getCategoryConfidence());
        result.setBreedConfidence(analysis.getBreedConfidence());
        result.setConfidence(analysis.getCategoryConfidence());
        result.setRawDetectLabel(analysis.getRawDetectLabel());
        result.setRawBreedLabel(analysis.getRawBreedLabel());
    }

    private void fillDictionaryResult(RecognitionResult result, Category category, Breed breed) {
        result.setCategoryId(category.getCategoryId());
        result.setCategoryCode(category.getCode());
        result.setCategoryName(category.getName());
        result.setBreedId(breed.getBreedId());
        result.setBreedCode(breed.getCode());
        result.setBreedName(breed.getName());
    }

    private Category resolveCategory(CatDogAnalysisResult analysis) {
        Category category = hasText(analysis.getCategoryCode())
                ? categoryMapper.selectCategoryByCode(analysis.getCategoryCode())
                : null;
        if (category == null || Boolean.FALSE.equals(category.getEnabled())) {
            throw new RuntimeException("未识别到有效的猫狗类别");
        }
        return category;
    }

    private Breed resolveBreed(String categoryId, String breedCode) {
        Breed breed = hasText(breedCode)
                ? breedMapper.selectBreedByCategoryIdAndCode(categoryId, breedCode)
                : null;
        if (breed != null) {
            return breed;
        }

        Breed defaultBreed = breedMapper.selectDefaultBreedByCategoryId(categoryId);
        if (defaultBreed == null) {
            throw new RuntimeException("未找到当前类别的默认品种");
        }
        return defaultBreed;
    }

    private String defaultMessage(String message, String fallback) {
        return message == null || message.isEmpty() ? fallback : message;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void deleteTempFile(File dest) {
        if (dest != null && dest.exists()) {
            dest.delete();
        }
    }
}
