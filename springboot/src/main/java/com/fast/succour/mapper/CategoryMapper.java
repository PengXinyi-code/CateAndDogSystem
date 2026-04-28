package com.fast.succour.mapper;

import com.fast.succour.domain.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
动物分类Mapper接口
 */
@Mapper
public interface CategoryMapper {
    /**查询动物分类列表
     * @param category 查询参数
     * @return 分类列表
     * */
    List<Category> selectCategoryList(Category category);

    Category selectCategoryByCategoryId(String categoryId);

    int insertCategory(Category category);

    int updateCategory(Category category);

    int deleteCategoryByCategoryIds(String[] categoryIds);
}
