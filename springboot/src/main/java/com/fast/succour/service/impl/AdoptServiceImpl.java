package com.fast.succour.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fast.succour.domain.AdoptionRecord;
import com.fast.succour.domain.Animal;
import com.fast.succour.mapper.AdoptionRecordMapper;
import com.fast.succour.service.IAnimalService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import com.fast.succour.domain.Adopt;
import com.fast.succour.service.IAdoptService;
import org.springframework.transaction.annotation.Transactional;
import com.fast.succour.mapper.AdoptMapper;
import static com.fast.system.general.utils.SecurityUtils.getUserId;

@Service
public class AdoptServiceImpl implements IAdoptService {

    @Resource
    private AdoptMapper adoptMapper;

    @Resource
    private IAnimalService animalService;

    @Resource
    private AdoptionRecordMapper adoptionRecordMapper;

    @Override
    public Adopt selectAdoptByAdoptId(String adoptId) {
        return adoptMapper.selectAdoptByAdoptId(adoptId);
    }

    @Override
    public List<Adopt> selectAdoptList(Adopt adopt) {
        return adoptMapper.selectAdoptList(adopt);
    }

    @Override
    @Transactional
    public int insertAdopt(Adopt adopt) {
        adopt.setCreateTime(new Date());
        adopt.setAdoptId(UUID.randomUUID().toString().replace("-", ""));
        adopt.setDescription("您的申请已被接受，工作人员会尽快与您联系安排后续事宜");
        adopt.setUserId(Math.toIntExact(getUserId()));

        String animalIdStr = adopt.getAnimalId();
        Animal animal = new Animal();
        animal.setId(Long.valueOf(animalIdStr));
        animal.setStatus("审核中");
        animalService.updateAnimal(animal);

        return adoptMapper.insertAdopt(adopt);
    }

    @Override
    @Transactional
    public int updateAdopt(Adopt adopt) {
        // 1. 更新申请状态
        int result = adoptMapper.updateAdopt(adopt);

        // 2. 如果审核通过（状态变为"已完成"），保存领养记录
        if ("已完成".equals(adopt.getStatus())) {
            // 获取完整的申请信息
            Adopt fullAdopt = adoptMapper.selectAdoptByAdoptId(adopt.getAdoptId());

            if (fullAdopt != null) {
                // 更新动物的领养状态
                Animal updateAnimal = new Animal();
                updateAnimal.setId(Long.valueOf(fullAdopt.getAnimalId()));
                updateAnimal.setIsAdopted(true);
                animalService.updateAnimal(updateAnimal);

                AdoptionRecord record = new AdoptionRecord();
                record.setAnimalId(fullAdopt.getAnimalId());
                record.setUserId(fullAdopt.getUserId());
                record.setUserName(fullAdopt.getName());
                record.setPhone(fullAdopt.getPhone());
                record.setEmail(fullAdopt.getEmail());
                record.setAddress(fullAdopt.getAddress());
                record.setOccupation(fullAdopt.getOccupation());
                record.setAdoptTime(new Date());

                // 获取动物名称
                try {
                    Animal animal = animalService.selectAnimalByAnimalId(Long.valueOf(fullAdopt.getAnimalId()));
                    if (animal != null) {
                        record.setAnimalName(animal.getName());
                    }
                } catch (Exception e) {
                    record.setAnimalName("");
                }

                adoptionRecordMapper.insertAdoptionRecord(record);
            }
        }

        return result;
    }

    @Override
    public int deleteAdoptByAdoptIds(String[] adoptIds) {
        return adoptMapper.deleteAdoptByAdoptIds(adoptIds);
    }

    @Override
    @Transactional
    public int revoke(String adoptId) {
        Adopt adopt = adoptMapper.selectAdoptByAdoptId(adoptId);
        if (adopt == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!"审核中".equals(adopt.getStatus())) {
            throw new RuntimeException("只有审核中的申请才能撤销");
        }

        Animal animal = new Animal();
        animal.setId(Long.valueOf(adopt.getAnimalId()));
        animal.setIsAdopted(false);
        animalService.updateAnimal(animal);

        return adoptMapper.deleteAdoptByAdoptIds(new String[]{adoptId});
    }
}