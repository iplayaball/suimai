package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.BrandDao;
import com.study.suimai.product.dao.CategoryBrandRelationDao;
import com.study.suimai.product.dao.CategoryDao;
import com.study.suimai.product.entity.BrandEntity;
import com.study.suimai.product.entity.CategoryBrandRelationEntity;
import com.study.suimai.product.entity.CategoryEntity;
import com.study.suimai.product.service.BrandService;
import com.study.suimai.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

  @Resource
  BrandDao brandDao;

  @Autowired
  BrandService brandService;

  @Resource
  CategoryDao categoryDao;

  @Resource
  CategoryBrandRelationDao relationDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<CategoryBrandRelationEntity> page = this.page(
     new Query<CategoryBrandRelationEntity>().getPage(params),
     new QueryWrapper<CategoryBrandRelationEntity>()
    );

    return new PageUtils(page);
  }

  @Override
  public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
    BrandEntity brandEntity = brandDao.selectById(categoryBrandRelation.getBrandId());
    CategoryEntity categoryEntity = categoryDao.selectById(categoryBrandRelation.getCatelogId());
    categoryBrandRelation.setBrandName(brandEntity.getName());
    categoryBrandRelation.setCatelogName(categoryEntity.getName());
    this.save(categoryBrandRelation);
  }

  @Override
  public void updateBrandNameByBrandId(Long brandId, String brandName) {
    CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
    entity.setBrandName(brandName);
    this
     .update(entity, new LambdaUpdateWrapper<CategoryBrandRelationEntity>()
      .eq(CategoryBrandRelationEntity::getBrandId, brandId));
  }

  @Override
  public void updateCategoryNameByCategoryId(Long catId, String categoryName) {
    CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
    entity.setCatelogName(categoryName);
    this
     .update(entity,
      new LambdaUpdateWrapper<CategoryBrandRelationEntity>()
       .eq(CategoryBrandRelationEntity::getCatelogId, catId));
  }

  @Override
  public List<BrandEntity> getBrandsByCatId(Long catId) {
    List<CategoryBrandRelationEntity> catelogId = relationDao
     .selectList(new QueryWrapper<CategoryBrandRelationEntity>()
      .eq("catelog_id", catId));
    List<BrandEntity> collect = catelogId.stream().map(item -> {
      Long brandId = item.getBrandId();
      return brandService.getById(brandId);
    }).collect(Collectors.toList());
    return collect;
  }
}