package com.fast.succour.service;

import com.fast.succour.domain.Animal;
import org.springframework.web.multipart.MultipartFile;

public interface RecognitionService {
    Animal recognize(MultipartFile file) throws Exception;
}
