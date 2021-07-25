package com.study.suimai.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.suimai.product.entity.AttrEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

  List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
