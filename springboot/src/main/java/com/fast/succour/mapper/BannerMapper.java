package com.fast.succour.mapper;

import com.fast.succour.domain.Banner;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BannerMapper
{
    /**
     * 查询轮播图
     *
     * @param bannerId 轮播图主键
     * @return 轮播图
     */
    public Banner selectBannerByBannerId(String bannerId);

    /**
     * 查询轮播图列表
     *
     * @param banner 轮播图
     * @return 轮播图集合
     */
    public List<Banner> selectBannerList(Banner banner);

    /**
     * 新增轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    public int insertBanner(Banner banner);

    /**
     * 修改轮播图
     *
     * @param banner 轮播图
     * @return 结果
     */
    public int updateBanner(Banner banner);

    /**
     * 批量删除轮播图
     *
     * @param bannerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBannerByBannerIds(String[] bannerIds);
}
