package com.fast.animal.controller;

import java.util.List;

import com.fast.animal.domain.AdoptionRecord;
import com.fast.animal.mapper.AdoptionRecordMapper;
import com.fast.system.domain.SysUser;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fast.system.general.core.controller.BaseController;
import com.fast.system.general.core.domain.AjaxResult;
import com.fast.animal.domain.Adopt;
import com.fast.animal.service.IAdoptService;
import com.fast.system.general.core.page.TableDataInfo;

import static com.fast.system.general.utils.SecurityUtils.getUserId;

@RestController
@RequestMapping("/api/adoptions")
public class AdoptController extends BaseController {

    @Resource
    private IAdoptService adoptService;

    @Resource
    private AdoptionRecordMapper adoptionRecordMapper;

    @GetMapping("/list")
    public TableDataInfo list(Adopt adopt) {
        if (!SysUser.isAdmin(getUserId())) {
            adopt.setUserId(Math.toIntExact(getUserId()));
        }
        startPage();
        List<Adopt> list = adoptService.selectAdoptList(adopt);
        return getDataTable(list);
    }

    @GetMapping(value = "/{adoptId}")
    public AjaxResult getInfo(@PathVariable("adoptId") String adoptId) {
        return success(adoptService.selectAdoptByAdoptId(adoptId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody Adopt adopt) {
        return toAjax(adoptService.insertAdopt(adopt));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody Adopt adopt) {
        return toAjax(adoptService.updateAdopt(adopt));
    }

    @DeleteMapping("/{adoptIds}")
    public AjaxResult remove(@PathVariable String[] adoptIds) {
        return toAjax(adoptService.deleteAdoptByAdoptIds(adoptIds));
    }

    @PutMapping("/revoke/{adoptId}")
    public AjaxResult revoke(@PathVariable String adoptId) {
        return toAjax(adoptService.revoke(adoptId));
    }

    // ========== 领养记录相关接口 ==========

    /**
     * 查询领养记录列表
     */
    @GetMapping("/record/list")
    public TableDataInfo recordList(AdoptionRecord record) {
        startPage();
        List<AdoptionRecord> list = adoptionRecordMapper.selectAdoptionRecordList(record);
        return getDataTable(list);
    }

    /**
     * 根据ID查询领养记录
     */
    @GetMapping("/record/{id}")
    public AjaxResult getRecordById(@PathVariable Long id) {
        return success(adoptionRecordMapper.selectAdoptionRecordById(id));
    }

    /**
     * 根据动物ID查询领养记录
     */
    @GetMapping("/record/animal/{animalId}")
    public AjaxResult getRecordByAnimalId(@PathVariable String animalId) {
        return success(adoptionRecordMapper.selectAdoptionRecordByAnimalId(animalId));
    }
}