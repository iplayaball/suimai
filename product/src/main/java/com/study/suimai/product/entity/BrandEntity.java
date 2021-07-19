package com.study.suimai.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    // TODO 新增修改分组校验
//	@NotNull(message = "修改必须指定ID")
//	@Null(message = "新增不能指定ID")
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名必须提交")
    private String name;
    /**
     * 品牌logo地址
     */
    // TODO 校验文件路径或者url
    //	@URL
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    // TODO 自定义校验注解
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母")
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull
    @Min(value = 0, message = "排序必须是大于等于0的整数")
    private Integer sort;

}
