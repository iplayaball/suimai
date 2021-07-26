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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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


  @CacheEvict(value = "category", allEntries = true)
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

  /**
   * 每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
   * 代表当前方法的结果需要缓存，如果缓存中有，方法都不用调用，如果缓存中没有，会调用方法。最后将方法的结果放入缓存
   * 默认行为
   * 如果缓存中有，方法不再调用
   * key是默认生成的:缓存的名字::SimpleKey::[](自动生成key值)
   * 缓存的value值，默认使用jdk序列化机制，将序列化的数据存到redis中
   * 默认时间是 -1：
   * <p>
   * 自定义操作：key的生成
   * 指定生成缓存的key：key属性指定，接收一个Spel
   * 指定缓存的数据的存活时间:配置文档中修改存活时间
   * 将数据保存为json格式
   * <p>
   * <p>
   * 4、Spring-Cache的不足之处：
   * 1）、读模式
   * 缓存穿透：查询一个null数据。解决方案：缓存空数据
   * 缓存击穿：大量并发进来同时查询一个正好过期的数据。解决方案：加锁 ? 默认是无加锁的;使用sync = true来解决击穿问题
   * 缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间
   * 2)、写模式：（缓存与数据库一致）
   * 1）、读写加锁。
   * 2）、引入Canal,感知到MySQL的更新去更新Redis
   * 3）、读多写多，直接去数据库查询就行
   * <p>
   * 总结：
   * 常规数据（读多写少，即时性，一致性要求不高的数据，完全可以使用Spring-Cache）：写模式(只要缓存的数据有过期时间就足够了)
   * 特殊数据：特殊设计
   * <p>
   * 原理：
   * CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
   */
  @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
  @Override
  public List<CategoryEntity> getLevel1Categorys() {
    List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
     new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    return categoryEntities;
  }

  @Cacheable(value = "category",key = "#root.method.name")
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

  private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parentCid) {
    List<CategoryEntity> categoryEntities = selectList.stream()
     .filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
    return categoryEntities;
    // return this.baseMapper.selectList(
    //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
  }
}