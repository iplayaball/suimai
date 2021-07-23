package com.study.suimai.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.suimai.product.entity.AttrAttrgroupRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

  void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}
