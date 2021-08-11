package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.AttrGroupDao;
import com.study.suimai.product.entity.AttrEntity;
import com.study.suimai.product.entity.AttrGroupEntity;
import com.study.suimai.product.service.AttrGroupService;
import com.study.suimai.product.service.AttrService;
import com.study.suimai.product.vo.AttrGroupWithAttrsVo;
import com.study.suimai.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

  @Autowired
  AttrService attrService;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<AttrGroupEntity> page = this.page(
     new Query<AttrGroupEntity>().getPage(params),
     new QueryWrapper<AttrGroupEntity>()
    );

    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params, long catelogId) {
    LambdaQueryWrapper<AttrGroupEntity> wrapper = new LambdaQueryWrapper<>();
    Object searchKey = params.get("key");
    wrapper.eq(catelogId != 0, AttrGroupEntity::getCatelogId, catelogId)
     // 搜索字符串等于id或者like分组名称
     .and(searchKey != null,
      wrapperKey -> {
        wrapperKey
         .eq(AttrGroupEntity::getAttrGroupId, searchKey)
         .or()
         .like(AttrGroupEntity::getAttrGroupName, searchKey);
      });
    IPage<AttrGroupEntity> page = this.page(
     new Query<AttrGroupEntity>().getPage(params),
     wrapper
    );

    return new PageUtils(page);
  }

  /**
   * 根据分类id查出所有的分组以及这些组里面的属性
   *
   * @param catelogId
   * @return
   */
  @Override
  public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
    //1、查询分组信息
    List<AttrGroupEntity> attrGroupEntities = this
     .list(new QueryWrapper<AttrGroupEntity>()
      .eq("catelog_id", catelogId));

    //2、查询所有属性
    List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream()
     .map(group -> {
       AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
       BeanUtils.copyProperties(group, attrsVo);
       List<AttrEntity> attrs = attrService.getRelationAttr(attrsVo.getAttrGroupId());
       attrsVo.setAttrs(attrs);
       return attrsVo;
     }).collect(Collectors.toList());

    return collect;


  }

  @Override
  public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
    //1、查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
    AttrGroupDao baseMapper = this.getBaseMapper();
    List<SpuItemAttrGroupVo> vos = baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);

    return vos;
  }

}