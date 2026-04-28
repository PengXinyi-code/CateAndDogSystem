package com.fast.succour.service;

import com.fast.succour.domain.Category;

import java.util.List;

/*
动物分类接口
 */
public interface ICategoryService {
    /**查询动物分类列表
     * @param category 查询参数
     * @return 分类列表
     * */
    List<Category> selectCategoryList(Category category);

    /**
     * 获取动物分类详细信息
     * @param categoryId 分类ID
     * @return 分类详细信息
     */
    Category selectCategoryByCategoryId(String categoryId);

    /**
     * 新增萌宠分类
     * @param category 萌宠分类（表单参数）
     * @return 是否新增成功
     */
    int insertCategory(Category category);

    /**
     * 修改动物信息
     * @param category
     * @return 是否修改成功
     */
    int updateCategory(Category category);

    /**
     * 删除宠物信息
     * @param categoryIds
     * @return 是否删除成功
     */
    int deleteCategoryByCategoryIds(String[] categoryIds);
}
