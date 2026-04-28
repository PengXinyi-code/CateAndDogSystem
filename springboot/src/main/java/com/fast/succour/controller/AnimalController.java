package com.fast.succour.controller;

import com.fast.succour.domain.Animal;
import com.fast.succour.service.AnimalService;
import com.fast.system.general.core.controller.BaseController;
import com.fast.system.general.core.domain.AjaxResult;
import com.fast.system.general.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/animal")
public class AnimalController extends BaseController {

    @Resource
    private AnimalService animalService;

    /**
     * 查询动物列表
     * GET /api/animal/list
     */
    @GetMapping("/list")
    public TableDataInfo list(Animal animal) {
        startPage();
        List<Animal> list = animalService.selectAnimalList(animal);
        return getDataTable(list);
    }

    /**
     * 根据ID查询动物
     * GET /api/animal/1
     */
    @GetMapping("/{id}")
    public AjaxResult findById(@PathVariable Long id) {
        return success(animalService.findById(id));
    }

    /**
     * 新增动物信息
     * POST /api/animal/add
     */
    @PostMapping("/add")
    public AjaxResult insertAnimal(
            @ModelAttribute Animal animal,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "imageUrl", required = false) String imageUrl
    ) {
        try {
            animalService.insertAnimal(animal, file, imageUrl);
            return success("添加成功");
        } catch (Exception e) {
            return error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 修改动物信息
     * PUT /api/animal
     */
    @PutMapping
    public AjaxResult updateAnimal(@RequestBody @Validated Animal animal) {
        return toAjax(animalService.updateAnimal(animal));
    }

    /**
     * 删除动物信息（支持批量）
     * DELETE /api/animal/1,2,3
     */
    @DeleteMapping("/{animalIds}")
    public AjaxResult remove(@PathVariable String[] animalIds) {
        return toAjax(animalService.deleteAnimalByAnimalIds(animalIds));
    }
}