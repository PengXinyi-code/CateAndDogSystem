package com.fast.succour.domain;

import lombok.Data;

@Data
public class Banner
{

    /** 轮播图ID */
    private String bannerId;

    /** 图片 */
    private String image;

    /** 排序 */
    private Long sort;


}