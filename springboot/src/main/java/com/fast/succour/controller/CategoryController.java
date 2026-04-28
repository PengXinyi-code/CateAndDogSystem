package com.fast.succour.controller;

import com.fast.succour.domain.Category;
import com.fast.succour.service.ICategoryService;
import com.fast.system.general.core.controller.BaseController;
import com.fast.system.general.core.domain.AjaxResult;
import com.fast.system.general.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
动物分类Controller
 */
@RestController
@RequestMapping("/sccour/category")
public class CategoryController extends BaseController {
    @Resource
    private ICategoryService categoryService;
    /*
    查询动物分类列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Category category) {
        startPage();
        List<Category> list = categoryService.selectCategoryList(category);
        return getDataTable(list);//返回前端
    }

    /**
     * 获取动物分类详细信息
     */
    @GetMapping("/{categoryId}")
    public AjaxResult selectCategoryByCategoryId(@PathVariable String categoryId) {
        return success(categoryService.selectCategoryByCategoryId(categoryId));
    }

    /**
     * 新增萌宠分类
     * @param category
     * @return
     */
    @PostMapping
    public AjaxResult insertCategory(@RequestBody Category category) {
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改动物信息
     */
    @PutMapping
    public AjaxResult updateCategory(@RequestBody Category category) {
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除动物信息
     */
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable String[] categoryIds) {
        return toAjax(categoryService.deleteCategoryByCategoryIds(categoryIds));
    }
}
