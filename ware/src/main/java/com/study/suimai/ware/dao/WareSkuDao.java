package com.study.suimai.ware.dao;

import com.study.suimai.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 13:52:14
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
