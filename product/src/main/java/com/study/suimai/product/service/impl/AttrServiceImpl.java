package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mysql.cj.util.StringUtils;
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
  AttrAttrgroupRelationDao attrAttrgroupRelationDao;

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
  public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
    LambdaQueryWrapper<AttrEntity> wrapper = new LambdaQueryWrapper<>();
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
      // 设置分组名称
      // 1 根据属性ID查出 AttrAttrgroupRelationEntity
      AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao
       .selectOne(new LambdaQueryWrapper<AttrAttrgroupRelationEntity>()
        .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));
      // 2 根据AttrAttrgroupRelationEntity 里的分类ID 查出attrGroupEntity
      if (attrAttrgroupRelationEntity != null) {
        Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);

        attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
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

  @Transactional
  @Override
  public void saveAttrVo(AttrVo attr) {
    AttrEntity attrEntity = new AttrEntity();
    BeanUtils.copyProperties(attr, attrEntity);

    this.save(attrEntity);

    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
    attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
    attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
    attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
  }

  @Override
  public AttrRespVo getDetailById(Long attrId) {
    // 查出基本信息
    AttrEntity attrEntity = this.getById(attrId);
    AttrRespVo attrRespVo = new AttrRespVo();
    BeanUtils.copyProperties(attrEntity, attrRespVo);

    // 分组信息
    // 1 根据属性ID查出 AttrAttrgroupRelationEntity
    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao
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