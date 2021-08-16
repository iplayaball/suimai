package com.study.suimai.product.dao;

import com.study.suimai.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.suimai.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

  List<SkuItemSaleAttrVo> getSaleAttrBySpuId(@Param("spuId") Long spuId);

  List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);
}
