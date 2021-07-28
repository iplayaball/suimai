package com.study.suimai.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.suimai.product.entity.SkuInfoEntity;
import com.study.suimai.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

  PageUtils queryPage(Map<String, Object> params);

  PageUtils queryPageByCondition(Map<String, Object> params);

  List<SkuInfoEntity> getSkusBySpuId(Long spuId);

  SkuItemVo item(Long skuId);
}

