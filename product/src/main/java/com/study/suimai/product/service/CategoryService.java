package com.study.suimai.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.suimai.product.entity.CategoryEntity;
import com.study.suimai.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
public interface CategoryService extends IService<CategoryEntity> {

  PageUtils queryPage(Map<String, Object> params);

  List<CategoryEntity> listTree();

  Long[] findCatelogPath(Long catelogId);

  void updateCascade(CategoryEntity category);

  List<CategoryEntity> getLevel1Categorys();

  Map<String, List<Catelog2Vo>> getCatalogJson();
}

