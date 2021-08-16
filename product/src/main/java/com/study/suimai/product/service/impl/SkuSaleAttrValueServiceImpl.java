package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.SkuSaleAttrValueDao;
import com.study.suimai.product.entity.SkuSaleAttrValueEntity;
import com.study.suimai.product.service.SkuSaleAttrValueService;
import com.study.suimai.product.vo.SkuItemSaleAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

  @Override
  public List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId) {
    SkuSaleAttrValueDao baseMapper = this.getBaseMapper();
    List<SkuItemSaleAttrVo> saleAttrVos = baseMapper.getSaleAttrBySpuId(spuId);

    return saleAttrVos;
  }

  @Override
  public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {

    SkuSaleAttrValueDao baseMapper = this.baseMapper;
    List<String> stringList = baseMapper.getSkuSaleAttrValuesAsStringList(skuId);

    return stringList;
  }

}