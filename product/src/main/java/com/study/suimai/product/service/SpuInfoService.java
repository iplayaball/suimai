package com.study.suimai.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.suimai.product.entity.SpuInfoEntity;
import com.study.suimai.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

  PageUtils queryPageByCondition(Map<String, Object> params);

  PageUtils queryPage(Map<String, Object> params);

  void saveSpuInfo(SpuSaveVo vo);
}

