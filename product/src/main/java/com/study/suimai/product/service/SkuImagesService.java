package com.study.suimai.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.suimai.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<SkuImagesEntity> getImagesBySkuId(Long skuId);
}

