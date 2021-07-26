package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.CategoryDao;
import com.study.suimai.product.entity.CategoryEntity;
import com.study.suimai.product.service.CategoryBrandRelationService;
import com.study.suimai.product.service.CategoryService;
import com.study.suimai.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

  @Autowired
  CategoryBrandRelationService categoryBrandRelationService;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<CategoryEntity> page = this.page(
     new Query<CategoryEntity>().getPage(params),
     new QueryWrapper<CategoryEntity>()
    );

    return new PageUtils(page);
  }

  @Override
  public List<CategoryEntity> listTree() {
    List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
    List<CategoryEntity> categoryTree = categoryEntityList.stream(
    ).filter(
     categoryEntity -> categoryEntity.getParentCid() == 0
    ).map(
     menu1 -> {
       menu1.setChildren(getChildren(menu1, categoryEntityList));
       return menu1;
     }
    ).sorted(
     Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
    ).collect(Collectors.toList());
    return categoryTree;
  }

  private List<CategoryEntity> getChildren(CategoryEntity menu1, List<CategoryEntity> categoryEntityList) {
    List<CategoryEntity> categoryChildrenTree = categoryEntityList.stream(
    ).filter(
     categoryEntity -> categoryEntity.getParentCid() == menu1.getCatId()
    ).map(
     menu -> {
       menu.setChildren(getChildren(menu, categoryEntityList));
       return menu;
     }
    ).sorted(
     Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
    ).collect(Collectors.toList());
    return categoryChildrenTree;
  }

  /**
   * 根据三级分类ID 获取完整的父分类数组
   *
   * @param catelogId
   * @return
   */
  @Override
  public Long[] findCatelogPath(Long catelogId) {
    ArrayList<Long> path = new ArrayList<>();

    findParentId(catelogId, path);

    Collections.reverse(path);
    return path.toArray(new Long[path.size()]);
  }

  private void findParentId(Long catelogId, ArrayList<Long> path) {
    path.add(catelogId);
    CategoryEntity categoryEntity = this.getById(catelogId);
    Long parentCid = categoryEntity.getParentCid();
    if (parentCid != 0) {
      findParentId(parentCid, path);
    }
  }


  @Override
  @Transactional
  public void updateCascade(CategoryEntity category) {
    this.updateById(category);

    String categoryName = category.getName();
    if (!StringUtils.isNullOrEmpty(categoryName)) {
      categoryBrandRelationService
       .updateCategoryNameByCategoryId(category.getCatId(), categoryName);
    }
  }

  @Override
  public List<CategoryEntity> getLevel1Categorys() {
    List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
     new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    return categoryEntities;
  }

  @Override
  public Map<String, List<Catelog2Vo>> getCatalogJson() {
    System.out.println("查询了数据库");

    //将数据库的多次查询变为一次
    List<CategoryEntity> selectList = this.baseMapper.selectList(null);

    //1、查出所有分类
    //1、1）查出所有一级分类
    List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

    //封装数据
    Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
      //1、每一个的一级分类,查到这个一级分类的二级分类
      List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

      //2、封装上面的结果
      List<Catelog2Vo> catelog2Vos = null;
      if (categoryEntities != null) {
        catelog2Vos = categoryEntities.stream().map(l2 -> {
          Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

          //1、找当前二级分类的三级分类封装成vo
          List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

          if (level3Catelog != null) {
            List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
              //2、封装成指定格式
              Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

              return category3Vo;
            }).collect(Collectors.toList());
            catelog2Vo.setCatalog3List(category3Vos);
          }

          return catelog2Vo;
        }).collect(Collectors.toList());
      }

      return catelog2Vos;
    }));

    return parentCid;
  }

  private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
    List<CategoryEntity> categoryEntities = selectList.stream()
     .filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
    return categoryEntities;
    // return this.baseMapper.selectList(
    //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
  }
}