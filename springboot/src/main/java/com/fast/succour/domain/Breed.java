package com.fast.succour.domain;

import lombok.Data;

/*
 * 猫狗品种对象，对应 breed 表
 */
@Data
public class Breed {
    private String breedId;
    private String categoryId;
    private String categoryName;
    private String code;
    private String name;
    private Integer sort;
    private Boolean enabled;
    private Boolean defaultBreed;
}
