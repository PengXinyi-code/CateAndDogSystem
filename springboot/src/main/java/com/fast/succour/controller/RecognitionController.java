package com.fast.succour.controller;

import com.fast.succour.domain.Animal;
import com.fast.succour.service.RecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/recognition")
public class RecognitionController {

    @Autowired
    private RecognitionService recognitionService;

    @PostMapping("/identify")
    public Object identify(@RequestParam("file") MultipartFile file) {
        try {
            Animal animal = recognitionService.recognize(file);

            if (animal != null) {
                return Map.of(
                        "matched", true,
                        "animal", animal
                );
            } else {
                return Map.of(
                        "matched", false,
                        "message", "未匹配到动物，请创建新档案"
                );
            }

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}