package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.SpuInfoDao;
import com.study.suimai.product.entity.*;
import com.study.suimai.product.service.*;
import com.study.suimai.product.vo.SpuSaveVo;
import com.study.suimai.product.vo.spusave.Attr;
import com.study.suimai.product.vo.spusave.BaseAttrs;
import com.study.suimai.product.vo.spusave.Images;
import com.study.suimai.product.vo.spusave.Skus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

  @Autowired
  SpuInfoDescService spuInfoDescService;

  @Autowired
  SpuImagesService imagesService;

  @Autowired
  AttrService attrService;

  @Autowired
  ProductAttrValueService attrValueService;

  @Autowired
  SkuInfoService skuInfoService;
  @Autowired
  SkuImagesService skuImagesService;

  @Autowired
  SkuSaleAttrValueService skuSaleAttrValueService;

  /**
   * //TODO 高级部分完善
   *
   * @param vo
   */
  @Transactional
  @Override
  public void saveSpuInfo(SpuSaveVo vo) {

    //1、保存spu基本信息 pms_spu_info
    SpuInfoEntity infoEntity = new SpuInfoEntity();
    BeanUtils.copyProperties(vo, infoEntity);
    infoEntity.setCreateTime(new Date());
    infoEntity.setUpdateTime(new Date());
    this.save(infoEntity);

    Long spuId = infoEntity.getId();
    Long brandId = infoEntity.getBrandId();
    Long catalogId = infoEntity.getCatalogId();

    //2、保存Spu的描述图片 pms_spu_info_desc
    List<String> decript = vo.getDecript();
    SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
    descEntity.setSpuId(spuId);
    descEntity.setDecript(String.join(",", decript));
    spuInfoDescService.save(descEntity);

    //3、保存spu的图片集 pms_spu_images
    List<String> images = vo.getImages();
    imagesService.saveImages(spuId, images);


    //4、保存spu的规格参数 pms_product_attr_value
    List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
    List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
      ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
      valueEntity.setAttrId(attr.getAttrId());
      AttrEntity id = attrService.getById(attr.getAttrId());
      valueEntity.setAttrName(id.getAttrName());
      valueEntity.setAttrValue(attr.getAttrValues());
      valueEntity.setQuickShow(attr.getShowDesc());
      valueEntity.setSpuId(spuId);

      return valueEntity;
    }).collect(Collectors.toList());
    attrValueService.saveBatch(collect);


    //5、保存spu的积分信息；suimai_sms->sms_spu_bounds TODO
    /*Bounds bounds = vo.getBounds();
    SpuBoundTo spuBoundTo = new SpuBoundTo();
    BeanUtils.copyProperties(bounds,spuBoundTo);
    spuBoundTo.setSpuId(spuId);
    R r = couponFeignService.saveSpuBounds(spuBoundTo);
    if(r.getCode() != 0){
      log.error("远程保存spu积分信息失败");
    }*/


    //5、保存当前spu对应的所有sku信息；
    List<Skus> skus = vo.getSkus();
    if (skus != null && skus.size() > 0) {
      skus.forEach(item -> {
        String defaultImg = "";
        for (Images image : item.getImages()) {
          if (image.getDefaultImg() == 1) {
            defaultImg = image.getImgUrl();
          }
        }
        //    private String skuName;
        //    private BigDecimal price;
        //    private String skuTitle;
        //    private String skuSubtitle;
        SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
        BeanUtils.copyProperties(item, skuInfoEntity);
        skuInfoEntity.setBrandId(brandId);
        skuInfoEntity.setCatalogId(catalogId);
        skuInfoEntity.setSaleCount(0L);
        skuInfoEntity.setSpuId(spuId);
        skuInfoEntity.setSkuDefaultImg(defaultImg);
        //5.1）、sku的基本信息；pms_sku_info
        skuInfoService.save(skuInfoEntity);

        Long skuId = skuInfoEntity.getSkuId();

        List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
          SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
          skuImagesEntity.setSkuId(skuId);
          skuImagesEntity.setImgUrl(img.getImgUrl());
          skuImagesEntity.setDefaultImg(img.getDefaultImg());
          return skuImagesEntity;
        }).filter(entity -> {
          //返回true就是需要，false就是剔除
          // 没有图片路径的无需保存
          return !StringUtils.isEmpty(entity.getImgUrl());
        }).collect(Collectors.toList());
        //5.2）、sku的图片信息；pms_sku_image
        skuImagesService.saveBatch(imagesEntities);

        List<Attr> attr = item.getAttr();
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream()
         .map(a -> {
           SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
           BeanUtils.copyProperties(a, attrValueEntity);
           attrValueEntity.setSkuId(skuId);

           return attrValueEntity;
         }).collect(Collectors.toList());
        //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
        skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

        // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price TODO
        /*SkuReductionTo skuReductionTo = new SkuReductionTo();
        BeanUtils.copyProperties(item, skuReductionTo);
        skuReductionTo.setSkuId(skuId);
        if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
          R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
          if (r1.getCode() != 0) {
            log.error("远程保存sku优惠信息失败");
          }
        }*/
      });
    }
  }

  @Override
  public PageUtils queryPageByCondition(Map<String, Object> params) {

    QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

    String key = (String) params.get("key");
    if (!StringUtils.isEmpty(key)) {
      wrapper.and((w) -> {
        w.eq("id", key).or().like("spu_name", key);
      });
    }
    // status=1 and (id=1 or spu_name like xxx)
    String status = (String) params.get("status");
    if (!StringUtils.isEmpty(status)) {
      wrapper.eq("publish_status", status);
    }

    String brandId = (String) params.get("brandId");
    if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
      wrapper.eq("brand_id", brandId);
    }

    String catelogId = (String) params.get("catelogId");
    if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
      wrapper.eq("catalog_id", catelogId);
    }

    /**
     * status: 2
     * key:
     * brandId: 9
     * catelogId: 225
     */

    IPage<SpuInfoEntity> page = this.page(
     new Query<SpuInfoEntity>().getPage(params),
     wrapper
    );

    return new PageUtils(page);
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<SpuInfoEntity> page = this.page(
     new Query<SpuInfoEntity>().getPage(params),
     new QueryWrapper<SpuInfoEntity>()
    );

    return new PageUtils(page);
  }
}