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


  @Override
  public List<Long> selectSearchAttrs(List<Long> attrIds) {
    List<Long> searchAttrIds = this.baseMapper.selectSearchAttrIds(attrIds);

    return searchAttrIds;
  }

  /**
   * ????????????
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
        // ??????????????????
        // 1 ????????????ID?????? AttrAttrgroupRelationEntity
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao
         .selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
          .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
        // 2 ??????AttrAttrgroupRelationEntity ????????????ID ??????attrGroupEntity
        if (attrAttrgroupRelationEntity != null) {
          Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
          AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);

          attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
        }
      }
      // ??????????????????
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
   * ???????????????ID???????????????
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
   * ????????????????????????????????????
   * ???????????????????????????????????????
   * ????????????????????????????????????
   * ????????????????????????????????????
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
      // ??????????????????????????? attr id ???????????????
      if (count != 0) {
        // ??????????????????????????? ?????? id
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

  // ????????????????????????????????????????????????
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

  // ????????????????????????
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
   * ?????????????????????????????????????????????
   *
   * @param params
   * @param attrgroupId
   * @return
   */
  @Override
  public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
    //1?????????????????????????????????????????????????????????????????????
    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
    Long catelogId = attrGroupEntity.getCatelogId();
    //2????????????????????????????????????????????????
    //2.1)?????????????????????????????????
    List<AttrGroupEntity> group = attrGroupDao
     .selectList(new QueryWrapper<AttrGroupEntity>()
      .eq("catelog_id", catelogId));
    List<Long> collect = group.stream()
     .map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

    //2.2)??????????????????????????????
    List<AttrAttrgroupRelationEntity> relationEntities = relationDao
     .selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
      .in("attr_group_id", collect));
    List<Long> attrIds = relationEntities.stream()
     .map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

    //2.3)???????????????????????????????????????????????????????????????
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
   * ????????????id?????????????????????????????????
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

  // ????????????????????????????????????
  @Override
  public AttrRespVo getDetailById(Long attrId) {
    // ??????????????????
    AttrEntity attrEntity = this.getById(attrId);
    AttrRespVo attrRespVo = new AttrRespVo();
    BeanUtils.copyProperties(attrEntity, attrRespVo);

    // ????????????
    // 1 ????????????ID?????? AttrAttrgroupRelationEntity
    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao
     .selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
      .eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
    // 2 ??????AttrAttrgroupRelationEntity ????????????ID ??????attrGroupEntity
    if (attrAttrgroupRelationEntity != null) {
      Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
      AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
      if (attrGroupEntity != null) {
        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
        attrRespVo.setAttrGroupId(attrGroupId);
      }
    }

    // ??????id????????????
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