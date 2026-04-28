package com.fast.succour.domain;

import lombok.Data;

/*
*动物分类对象 对应category表
*/
@Data
public class Category {
    private String  categoryId;//分类ID
    private String  name;//分类名称
    private Integer sort;//排序
}
