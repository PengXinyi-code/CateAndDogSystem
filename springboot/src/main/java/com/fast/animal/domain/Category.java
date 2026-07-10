package com.fast.animal.domain;

import lombok.Data;

/*
*动物分类对象 对应category表
*/
@Data
public class Category {
    private String  categoryId;//分类ID
    private String  code;//分类编码
    private String  name;//分类名称
    private Integer sort;//排序
    private Boolean enabled;//是否启用
}
