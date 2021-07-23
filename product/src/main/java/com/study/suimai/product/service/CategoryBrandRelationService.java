package com.study.suimai.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.suimai.product.entity.BrandEntity;
import com.study.suimai.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

  PageUtils queryPage(Map<String, Object> params);

  void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

  void updateBrandNameByBrandId(Long brandId, String brandName);

  void updateCategoryNameByCategoryId(Long catId, String categoryName);

  List<BrandEntity> getBrandsByCatId(Long catId);
}

