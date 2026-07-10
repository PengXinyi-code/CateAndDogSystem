package com.fast.animal.controller;

import com.fast.animal.domain.Breed;
import com.fast.animal.service.IBreedService;
import com.fast.system.general.core.controller.BaseController;
import com.fast.system.general.core.domain.AjaxResult;
import com.fast.system.general.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breeds")
public class BreedController extends BaseController {
    @Resource
    private IBreedService breedService;

    @GetMapping("/list")
    public TableDataInfo list(Breed breed) {
        startPage();
        List<Breed> list = breedService.selectBreedList(breed);
        return getDataTable(list);
    }

    @GetMapping("/{breedId}")
    public AjaxResult getInfo(@PathVariable String breedId) {
        return success(breedService.selectBreedByBreedId(breedId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody Breed breed) {
        return toAjax(breedService.insertBreed(breed));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody Breed breed) {
        return toAjax(breedService.updateBreed(breed));
    }

    @DeleteMapping("/{breedIds}")
    public AjaxResult remove(@PathVariable String[] breedIds) {
        return toAjax(breedService.deleteBreedByBreedIds(breedIds));
    }
}
