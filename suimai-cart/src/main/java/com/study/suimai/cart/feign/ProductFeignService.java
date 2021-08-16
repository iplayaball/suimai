package com.study.suimai.cart.feign;

import com.study.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@FeignClient("product")
public interface ProductFeignService {

  /**
   * 根据skuId查询sku信息
   *
   * @param skuId
   * @return
   */
  @RequestMapping("/product/skuinfo/info/{skuId}")
  R getInfo(@PathVariable("skuId") Long skuId);

  /**
   * 根据skuId查询pms_sku_sale_attr_value表中的信息
   *
   * @param skuId
   * @return
   */
  @GetMapping(value = "/product/skusaleattrvalue/stringList/{skuId}")
  List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

}
