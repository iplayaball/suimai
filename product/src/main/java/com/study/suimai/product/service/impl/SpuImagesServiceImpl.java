package com.study.suimai.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.product.dao.SpuImagesDao;
import com.study.suimai.product.entity.SpuImagesEntity;
import com.study.suimai.product.service.SpuImagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

  @Override
  public void saveImages(Long spuId, List<String> images) {
    if(images == null || images.size() == 0){

    }else{
      List<SpuImagesEntity> collect = images.stream().map(img -> {
        SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
        spuImagesEntity.setSpuId(spuId);
        spuImagesEntity.setImgUrl(img);

        return spuImagesEntity;
      }).collect(Collectors.toList());

      this.saveBatch(collect);
    }
  }

}