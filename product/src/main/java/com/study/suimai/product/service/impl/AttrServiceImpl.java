package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.AttrDao;
import com.study.suimai.product.entity.AttrAttrgroupRelationEntity;
import com.study.suimai.product.entity.AttrEntity;
import com.study.suimai.product.service.AttrAttrgroupRelationService;
import com.study.suimai.product.service.AttrService;
import com.study.suimai.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

  @Autowired
  AttrAttrgroupRelationService attrAttrgroupRelationService;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<AttrEntity> page = this.page(
     new Query<AttrEntity>().getPage(params),
     new QueryWrapper<AttrEntity>()
    );

    return new PageUtils(page);
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

}