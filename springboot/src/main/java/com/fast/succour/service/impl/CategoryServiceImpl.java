package com.fast.succour.service.impl;

import com.fast.succour.domain.Category;
import com.fast.succour.mapper.CategoryMapper;
import com.fast.succour.service.ICategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/*
动物分类业务层
 */
@Service
public class CategoryServiceImpl implements ICategoryService {
    @Resource
    private CategoryMapper categoryMapper;
    /**
     * 查询动物分类列表
     * @param category 查询参数
     * @return 分类列表
     */
    @Override
    public List<Category> selectCategoryList(Category category) {
        return categoryMapper.selectCategoryList(category);
    }

    /**
     * 获取动物分类详细信息
     * @param categoryId 分类ID
     * @return 分类详细信息
     */
    @Override
    public Category selectCategoryByCategoryId(String categoryId) {
        return categoryMapper.selectCategoryByCategoryId(categoryId);
    }

    /**
     * 新增动物分类
     * @param category 萌宠分类（表单参数）
     * @return 是否新建成功
     */
    @Override
    public int insertCategory(Category category) {
        throw new RuntimeException("系统一级分类已固定为猫和狗，请在品种字典中维护细分类");
    }

    @Override
    public int updateCategory(Category category) {
        if (isCoreCategory(category.getCategoryId()) && Boolean.FALSE.equals(category.getEnabled())) {
            throw new RuntimeException("猫狗核心分类不能停用");
        }
        return categoryMapper.updateCategory(category);
    }

    @Override
    public int deleteCategoryByCategoryIds(String[] categoryIds) {
        for (String categoryId : categoryIds) {
            if (isCoreCategory(categoryId)) {
                throw new RuntimeException("猫狗核心分类不能删除");
            }
        }
        return categoryMapper.deleteCategoryByCategoryIds(categoryIds);
    }

    private boolean isCoreCategory(String categoryId) {
        return "cat".equals(categoryId) || "dog".equals(categoryId);
    }
}
