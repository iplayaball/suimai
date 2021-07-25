package com.study.suimai.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.suimai.product.entity.AttrEntity;
import com.study.suimai.product.vo.AttrGroupRelationVo;
import com.study.suimai.product.vo.AttrRespVo;
import com.study.suimai.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
public interface AttrService extends IService<AttrEntity> {

  PageUtils queryPage(Map<String, Object> params);

  void saveAttrVo(AttrVo attr);

  PageUtils queryPage(Map<String, Object> params, String attrType, Long catelogId);

  AttrRespVo getDetailById(Long attrId);

  void updateCascade(AttrVo attrvo);

  List<AttrEntity> getByGroupId(Long attrgroupId);

  void deleteRelation(List<AttrGroupRelationVo> vos);

  PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

  List<AttrEntity> getRelationAttr(Long attrGroupId);

  List<Long> selectSearchAttrs(List<Long> attrIds);
}

