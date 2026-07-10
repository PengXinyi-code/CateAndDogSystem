package com.fast.animal.service;

import com.fast.animal.domain.Animal;
import com.fast.animal.domain.RecognitionResult;
import org.springframework.web.multipart.MultipartFile;

public interface RecognitionService {
    RecognitionResult recognize(MultipartFile file) throws Exception;
}
