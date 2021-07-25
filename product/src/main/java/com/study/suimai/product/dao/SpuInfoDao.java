package com.study.suimai.product.dao;

import com.study.suimai.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

  void updaSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
