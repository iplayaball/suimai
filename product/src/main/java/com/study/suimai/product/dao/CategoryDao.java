package com.study.suimai.product.dao;

import com.study.suimai.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
