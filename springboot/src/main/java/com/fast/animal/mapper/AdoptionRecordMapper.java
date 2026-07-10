package com.fast.animal.mapper;

import com.fast.animal.domain.AdoptionRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AdoptionRecordMapper {

    // 插入领养记录
    int insertAdoptionRecord(AdoptionRecord record);

    // 查询领养记录列表
    List<AdoptionRecord> selectAdoptionRecordList(AdoptionRecord record);

    // 根据ID查询领养记录
    AdoptionRecord selectAdoptionRecordById(Long id);

    // 根据动物ID查询领养记录
    AdoptionRecord selectAdoptionRecordByAnimalId(String animalId);
}