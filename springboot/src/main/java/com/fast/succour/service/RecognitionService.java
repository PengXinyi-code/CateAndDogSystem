package com.fast.succour.service;

import com.fast.succour.domain.Animal;
import com.fast.succour.domain.RecognitionResult;
import org.springframework.web.multipart.MultipartFile;

public interface RecognitionService {
    RecognitionResult recognize(MultipartFile file) throws Exception;
}
