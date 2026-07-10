package com.fast.animal.domain;

import com.fast.system.general.core.domain.BaseEntity;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 宠物对象 adopt
 *
 * @author huacai
 * @date 2025-12-16
 */

@Data
public class Adopt {
    /** 领养申请ID */
    private String adoptId;

    /** 宠物ID */
    private String animalId;

    /** 申请人姓名 */
    private String name;

    /** 联系电话 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 居住地址 */
    private String address;

    /** 职业 */
    private String occupation;

    /** 申请理由 */
    private String reason;

    /** 养宠经验 */
    private String petExperience;

    /** 申请状态 */
    private String status;

    /** 处理说明 */
    private String description;

    /** 申请用户ID */
    private Integer userId;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    //宠物姓名
    private String animalName;

    //申请用户
    private String userName;


}
