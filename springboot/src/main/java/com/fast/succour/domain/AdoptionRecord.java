package com.fast.succour.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class AdoptionRecord {
    private Long id;
    private String animalId;
    private String animalName;
    private Integer userId;
    private String userName;
    private String phone;
    private String email;
    private String address;
    private String occupation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date adoptTime;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}