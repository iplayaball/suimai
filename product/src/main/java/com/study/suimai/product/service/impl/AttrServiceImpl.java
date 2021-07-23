package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
import com.study.common.constant.ProductConstant;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.AttrAttrgroupRelationDao;
import com.study.suimai.product.dao.AttrDao;
import com.study.suimai.product.dao.AttrGroupDao;
import com.study.suimai.product.dao.CategoryDao;
import com.study.suimai.product.entity.AttrAttrgroupRelationEntity;
import com.study.suimai.product.entity.AttrEntity;
import com.study.suimai.product.entity.AttrGroupEntity;
import com.study.suimai.product.entity.CategoryEntity;
import com.study.suimai.product.service.AttrAttrgroupRelationService;
import com.study.suimai.product.service.AttrService;
import com.study.suimai.product.service.CategoryService;
import com.study.suimai.product.vo.AttrGroupRelationVo;
import com.study.suimai.product.vo.AttrRespVo;
import com.study.suimai.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

  @Autowired
  AttrAttrgroupRelationService attrAttrgroupRelationService;

  @Autowired
  CategoryService categoryService;

  @Resource
  AttrAttrgroupRelationDao relationDao;

  @Resource
  AttrGroupDao attrGroupDao;

  @Resource
  CategoryDao categoryDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<AttrEntity> page = this.page(
     new Query<AttrEntity>().getPage(params),
     new QueryWrapper<AttrEntity>()
    );

    return new PageUtils(page);
  }

  /**
   * 分页列表
   *
   * @param params
   * @param attrType
   * @param catelogId
   * @return
   */
  @Override
  public PageUtils queryPage(Map<String, Object> params, String attrType, Long catelogId) {
    boolean isBaseType = "base".equalsIgnoreCase(attrType);
    LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(AttrEntity::getAttrType,
     isBaseType ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

    wrapper.eq(catelogId != 0, AttrEntity::getCatelogId, catelogId);
    String searchKey = (String) params.get("key");
    wrapper.and(!StringUtils.isNullOrEmpty(searchKey),
     wrapper1 -> {
       wrapper1
        .eq(AttrEntity::getAttrId, searchKey)
        .or()
        .like(AttrEntity::getAttrName, searchKey);
     });

    IPage<AttrEntity> page = this.page(
     new Query<AttrEntity>().getPage(params),
     wrapper
    );

    List<AttrEntity> records = page.getRecords();

    List<AttrRespVo> attrRespVoList = records.stream().map(attrEntity -> {
      AttrRespVo attrRespVo = new AttrRespVo();
      BeanUtils.copyProperties(attrEntity, attrRespVo);
      if (isBaseType) {
        // 设置分组名称
        // 1 根据属性ID查出 AttrAttrgroupRelationEntity
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao
         .selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
          .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
        // 2 根据AttrAttrgroupRelationEntity 里的分类ID 查出attrGroupEntity
        if (attrAttrgroupRelationEntity != null) {
          Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
          AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);

          attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
        }
      }
      // 设置分类名称
      CategoryEntity categoryEntity = categoryDao.selectById(attrRespVo.getCatelogId());
      if (categoryEntity != null) {
        attrRespVo.setCatelogName(categoryEntity.getName());
      }
      return attrRespVo;
    }).collect(Collectors.toList());

    PageUtils pageUtils = new PageUtils(page);
    pageUtils.setList(attrRespVoList);

    return pageUtils;
  }

  /**
   * 保存（分组ID在中间表）
   *
   * @param attr
   */
  @Transactional
  @Override
  public void saveAttrVo(AttrVo attr) {
    AttrEntity attrEntity = new AttrEntity();
    BeanUtils.copyProperties(attr, attrEntity);

    this.save(attrEntity);

    if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
      AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
      attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
      attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
      attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
    }
  }

  /**
   * 修改（如果有是基本属性，
   * 那就查看是否有关联的分组，
   * 有的话就更新关联的分组，
   * 没有的话就新建一个关联）
   *
   * @param attrvo
   */
  @Transactional
  @Override
  public void updateCascade(AttrVo attrvo) {
    AttrEntity attrEntity = new AttrEntity();
    BeanUtils.copyProperties(attrvo, attrEntity);

    this.updateById(attrEntity);

    if (attrvo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
      AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
      attrAttrgroupRelationEntity.setAttrGroupId(attrvo.getAttrGroupId());

      int count = attrAttrgroupRelationService
       .count(new QueryWrapper<AttrAttrgroupRelationEntity>()
        .eq("attr_id", attrEntity.getAttrId()));
      // 判断中间表是否有此 attr id 关联的记录
      if (count != 0) {
        // 有的话就更新关联的 分组 id
        attrAttrgroupRelationService
         .update(attrAttrgroupRelationEntity,
          new UpdateWrapper<AttrAttrgroupRelationEntity>()
           .eq("attr_id", attrEntity.getAttrId()));
      } else {
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
        attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
      }
    }
  }

  // 获取一个分组下已经关联的所有属性
  @Override
  public List<AttrEntity> getByGroupId(Long attrgroupId) {
    List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationService
     .list(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
      .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrgroupId));

    List<Long> ids = relationEntities.stream()
     .map(AttrAttrgroupRelationEntity::getAttrId)
     .collect(Collectors.toList());

    if (ids.size() == 0){
      return null;
    }
    return this.listByIds(ids);
  }

  // 批量删除关联关系
  @Override
  public void deleteRelation(List<AttrGroupRelationVo> vos) {
    List<AttrAttrgroupRelationEntity> entities = vos.stream()
     .map((item) -> {
       AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
       BeanUtils.copyProperties(item, relationEntity);
       return relationEntity;
     }).collect(Collectors.toList());
    relationDao.deleteBatchRelation(entities);
  }

  /**
   * 获取当前分组没有关联的所有属性
   *
   * @param params
   * @param attrgroupId
   * @return
   */
  @Override
  public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
    //1、当前分组只能关联自己所属的分类里面的所有属性
    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
    Long catelogId = attrGroupEntity.getCatelogId();
    //2、当前分组只能关联没有引用的属性
    //2.1)、当前分类下的所有分组
    List<AttrGroupEntity> group = attrGroupDao
     .selectList(new QueryWrapper<AttrGroupEntity>()
      .eq("catelog_id", catelogId));
    List<Long> collect = group.stream()
     .map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

    //2.2)、这些分组关联的属性
    List<AttrAttrgroupRelationEntity> relationEntities = relationDao
     .selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
      .in("attr_group_id", collect));
    List<Long> attrIds = relationEntities.stream()
     .map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

    //2.3)、从当前分类的所有基本属性中移除这些属性；
    QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
     .eq("catelog_id", catelogId)
     .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
     .notIn(attrIds != null && attrIds.size() > 0, "attr_id", attrIds);

    String key = (String) params.get("key");
    if (!StringUtils.isNullOrEmpty(key)) {
      wrapper.and((w) -> {
        w.eq("attr_id", key).or().like("attr_name", key);
      });
    }
    IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

    PageUtils pageUtils = new PageUtils(page);

    return pageUtils;
  }

  /**
   * 根据分组id查找关联的所有基本属性
   * @param attrgroupId
   * @return
   */
  @Override
  public List<AttrEntity> getRelationAttr(Long attrgroupId) {
    List<AttrAttrgroupRelationEntity> entities = relationDao
     .selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
     .eq("attr_group_id", attrgroupId));

    List<Long> attrIds = entities.stream()
     .map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

    if(attrIds.size() == 0){
      return null;
    }
    return this.listByIds(attrIds);
  }

  // 修改表单时，回显详细信息
  @Override
  public AttrRespVo getDetailById(Long attrId) {
    // 查出基本信息
    AttrEntity attrEntity = this.getById(attrId);
    AttrRespVo attrRespVo = new AttrRespVo();
    BeanUtils.copyProperties(attrEntity, attrRespVo);

    // 分组信息
    // 1 根据属性ID查出 AttrAttrgroupRelationEntity
    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao
     .selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
      .eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
    // 2 根据AttrAttrgroupRelationEntity 里的分类ID 查出attrGroupEntity
    if (attrAttrgroupRelationEntity != null) {
      Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
      AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
      if (attrGroupEntity != null) {
        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
        attrRespVo.setAttrGroupId(attrGroupId);
      }
    }

    // 分类id完整路径
    Long catelogId = attrEntity.getCatelogId();
    Long[] catelogPath = categoryService.findCatelogPath(catelogId);
    attrRespVo.setCatelogPath(catelogPath);

    CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
    if (categoryEntity != null) {
      attrRespVo.setCatelogName(categoryEntity.getName());
    }

    return attrRespVo;
  }
}