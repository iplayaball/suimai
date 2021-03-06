package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.constant.ProductConstant;
import com.study.common.to.es.SkuEsModel;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.common.utils.R;
import com.study.suimai.product.dao.SpuInfoDao;
import com.study.suimai.product.entity.*;
import com.study.suimai.product.feign.SearchFeignService;
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

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  @Autowired
  private ProductAttrValueService productAttrValueService;

  @Autowired
  private BrandService brandService;

  @Autowired
  private CategoryService categoryService;

  @Resource
  SearchFeignService searchFeignService;


  //  @GlobalTransactional(rollbackFor = Exception.class)
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void up(Long spuId) {

    //1???????????????spuId???????????????sku??????,???????????????
    List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

    // 4???????????????sku?????????????????????????????????????????????
    // ????????????spu ????????? ????????????
    List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListforspu(spuId);

    List<Long> attrIds = baseAttrs.stream().map(attr -> {
      return attr.getAttrId();
    }).collect(Collectors.toList());

    // ??????????????? ??????????????????????????????
    List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
    //?????????Set??????
    Set<Long> idSet = searchAttrIds.stream().collect(Collectors.toSet());

    // ??? spu ????????????????????????????????????????????????
    List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
      return idSet.contains(item.getAttrId());
    }).map(item -> {
      SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
      BeanUtils.copyProperties(item, attrs);
      return attrs;
    }).collect(Collectors.toList());


    /*List<Long> skuIdList = skuInfoEntities.stream()
     .map(SkuInfoEntity::getSkuId)
     .collect(Collectors.toList());
    //TODO 1?????????????????????????????????????????????????????????
    Map<Long, Boolean> stockMap = null;
    try {
      R skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
      //
      TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
      };
      stockMap = skuHasStock.getData(typeReference).stream()
       .collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
    } catch (Exception e) {
      log.error("?????????????????????????????????{}", e);
    }
    Map<Long, Boolean> finalStockMap = stockMap;
    */

    //2???????????????sku?????????
    List<SkuEsModel> collect = skuInfoEntities.stream().map(sku -> {
      //?????????????????????
      SkuEsModel esModel = new SkuEsModel();
      esModel.setSkuPrice(sku.getPrice());
      esModel.setSkuImg(sku.getSkuDefaultImg());

      //??????????????????
      esModel.setHasStock(true);
      /*if (finalStockMap == null) {
        esModel.setHasStock(true);
      } else {
        esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
      }*/

      //TODO 2??????????????????0
      esModel.setHotScore(0L);

      // ????????????????????????????????????
      BrandEntity brandEntity = brandService.getById(sku.getBrandId());
      esModel.setBrandName(brandEntity.getName());
      esModel.setBrandId(brandEntity.getBrandId());
      esModel.setBrandImg(brandEntity.getLogo());

      CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
      esModel.setCatalogId(categoryEntity.getCatId());
      esModel.setCatalogName(categoryEntity.getName());

      //??????????????????
      esModel.setAttrs(attrsList);

      BeanUtils.copyProperties(sku, esModel);

      return esModel;
    }).collect(Collectors.toList());

    //3??????????????????es???????????????gulimall-search
    R r = searchFeignService.productStatusUp(collect);

    if (r.getCode() == 0) {
      //??????????????????
      // 6???????????????spu?????????
      this.baseMapper.updaSpuStatus(spuId, ProductConstant.ProductStatusEnum.SPU_UP.getCode());
    } else {
      //??????????????????
      //TODO 7?????????????????????????????????:????????????
    }
  }

  /**
   * //TODO ??????????????????
   *
   * @param vo
   */
  @Transactional
  @Override
  public void saveSpuInfo(SpuSaveVo vo) {

    //1?????????spu???????????? pms_spu_info
    SpuInfoEntity infoEntity = new SpuInfoEntity();
    BeanUtils.copyProperties(vo, infoEntity);
    infoEntity.setCreateTime(new Date());
    infoEntity.setUpdateTime(new Date());
    this.save(infoEntity);

    Long spuId = infoEntity.getId();
    Long brandId = infoEntity.getBrandId();
    Long catalogId = infoEntity.getCatalogId();

    //2?????????Spu??????????????? pms_spu_info_desc
    List<String> decript = vo.getDecript();
    SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
    descEntity.setSpuId(spuId);
    descEntity.setDecript(String.join(",", decript));
    spuInfoDescService.save(descEntity);

    //3?????????spu???????????? pms_spu_images
    List<String> images = vo.getImages();
    imagesService.saveImages(spuId, images);


    //4?????????spu??????????????? pms_product_attr_value
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


    //5?????????spu??????????????????suimai_sms->sms_spu_bounds TODO
    /*Bounds bounds = vo.getBounds();
    SpuBoundTo spuBoundTo = new SpuBoundTo();
    BeanUtils.copyProperties(bounds,spuBoundTo);
    spuBoundTo.setSpuId(spuId);
    R r = couponFeignService.saveSpuBounds(spuBoundTo);
    if(r.getCode() != 0){
      log.error("????????????spu??????????????????");
    }*/


    //5???????????????spu???????????????sku?????????
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
        //5.1??????sku??????????????????pms_sku_info
        skuInfoService.save(skuInfoEntity);

        Long skuId = skuInfoEntity.getSkuId();

        List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
          SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
          skuImagesEntity.setSkuId(skuId);
          skuImagesEntity.setImgUrl(img.getImgUrl());
          skuImagesEntity.setDefaultImg(img.getDefaultImg());
          return skuImagesEntity;
        }).filter(entity -> {
          //??????true???????????????false????????????
          // ?????????????????????????????????
          return !StringUtils.isEmpty(entity.getImgUrl());
        }).collect(Collectors.toList());
        //5.2??????sku??????????????????pms_sku_image
        skuImagesService.saveBatch(imagesEntities);

        List<Attr> attr = item.getAttr();
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream()
         .map(a -> {
           SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
           BeanUtils.copyProperties(a, attrValueEntity);
           attrValueEntity.setSkuId(skuId);

           return attrValueEntity;
         }).collect(Collectors.toList());
        //5.3??????sku????????????????????????pms_sku_sale_attr_value
        skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

        // //5.4??????sku??????????????????????????????gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price TODO
        /*SkuReductionTo skuReductionTo = new SkuReductionTo();
        BeanUtils.copyProperties(item, skuReductionTo);
        skuReductionTo.setSkuId(skuId);
        if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
          R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
          if (r1.getCode() != 0) {
            log.error("????????????sku??????????????????");
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