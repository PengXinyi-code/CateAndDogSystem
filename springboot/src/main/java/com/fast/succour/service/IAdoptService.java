package com.fast.succour.service;

import java.util.List;
import com.fast.succour.domain.Adopt;

/**
 * 领养申请Service接口
 *
 * @author huacai
 * @date 2025-12-16
 */
public interface IAdoptService
{
    /**
     * 查询领养申请
     *
     * @param adoptId 领养申请主键
     * @return 领养申请
     */
    public Adopt selectAdoptByAdoptId(String adoptId);

    /**
     * 查询领养申请列表
     *
     * @param adopt 领养申请
     * @return 领养申请集合
     */
    public List<Adopt> selectAdoptList(Adopt adopt);

    /**
     * 新增领养申请
     *
     * @param adopt 领养申请
     * @return 结果
     */
    public int insertAdopt(Adopt adopt);

    /**
     * 修改领养申请
     *
     * @param adopt 领养申请
     * @return 结果
     */
    public int updateAdopt(Adopt adopt);

    /**
     * 批量删除领养申请
     *
     * @param adoptIds 需要删除的领养申请主键集合
     * @return 结果
     */
    public int deleteAdoptByAdoptIds(String[] adoptIds);

    /**
     * 撤销申请
     * @param adoptId
     * @return
     */
    int revoke(String adoptId);
}
