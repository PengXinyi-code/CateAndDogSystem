package com.fast.succour.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class Animal {
    private Long id;
    private String name;
    private String species;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")   // 反序列化 JSON 入参用
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 表单参数绑定用
    private LocalDateTime firstFoundTime;
    private Boolean isAdopted;
    private String status;
    private String location;
    private String imageUrl;
}