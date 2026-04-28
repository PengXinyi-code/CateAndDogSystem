package com.fast.succour.controller;

import java.util.List;

import com.fast.succour.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fast.system.general.core.controller.BaseController;
import com.fast.system.general.core.domain.AjaxResult;

import com.fast.succour.domain.Banner;
import com.fast.succour.service.BannerService;
import com.fast.system.general.core.page.TableDataInfo;

/**
 * 轮播图Controller
 *
 * @author huacai
 * @date 2025-12-16
 */
@RestController
@RequestMapping("/succour/banner")
public class BannerController extends BaseController {
    @Resource
    private BannerService bannerService;

    /**
     * 查询轮播图列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Banner banner) {
        startPage();
        List<Banner> list = bannerService.selectBannerList(banner);
        return getDataTable(list);
    }

    /**
     * 获取轮播图详细信息
     */
    @GetMapping(value = "/{bannerId}")
    public AjaxResult getInfo(@PathVariable("bannerId") String bannerId) {
        return success(bannerService.selectBannerByBannerId(bannerId));
    }

    /**
     * 新增轮播图
     */
    @PostMapping
    public AjaxResult add(@RequestBody Banner banner) {
        return toAjax(bannerService.insertBanner(banner));
    }

    /**
     * 修改轮播图
     */
    @PutMapping
    public AjaxResult edit(@RequestBody Banner banner) {
        return toAjax(bannerService.updateBanner(banner));
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/{bannerIds}")
    public AjaxResult remove(@PathVariable String[] bannerIds) {
        return toAjax(bannerService.deleteBannerByBannerIds(bannerIds));
    }
}
