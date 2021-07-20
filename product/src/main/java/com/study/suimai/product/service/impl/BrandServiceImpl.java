package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.BrandDao;
import com.study.suimai.product.entity.BrandEntity;
import com.study.suimai.product.service.BrandService;
import com.study.suimai.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

  @Autowired
  CategoryBrandRelationService categoryBrandRelationService;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {

    String searchKey = (String) params.get("key");

    LambdaQueryWrapper<BrandEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.and(!StringUtils.isNullOrEmpty(searchKey),
     wrapper1 -> {
       wrapper1
        .eq(BrandEntity::getBrandId, searchKey)
        .or()
        .like(BrandEntity::getName, searchKey);
     });

    IPage<BrandEntity> page = this.page(
     new Query<BrandEntity>().getPage(params),
     wrapper
    );

    return new PageUtils(page);
  }

  @Override
  public void updateCascade(BrandEntity brand) {
    this.updateById(brand);

    String brandName = brand.getName();
    if (!StringUtils.isNullOrEmpty(brandName)) {
      categoryBrandRelationService
       .updateBrandNameByBrandId(brand.getBrandId(), brandName);
    }

  }

}