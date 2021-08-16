package com.study.suimai.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.common.utils.R;
import com.study.suimai.cart.feign.ProductFeignService;
import com.study.suimai.cart.interceptor.CartInterceptor;
import com.study.suimai.cart.service.CartService;
import com.study.suimai.cart.to.UserInfoTo;
import com.study.suimai.cart.vo.CartItemVo;
import com.study.suimai.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static com.study.common.constant.CartConstant.CART_PREFIX;


@Slf4j
@Service("cartService")
public class CartServiceImpl implements CartService {

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private ProductFeignService productFeignService;

  @Autowired
  private ThreadPoolExecutor executor;

  @Override
  public CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

    //拿到要操作的购物车信息
    BoundHashOperations<String, Object, Object> cartOps = getCartOps();

    //判断Redis是否有该商品的信息
    String productRedisValue = (String) cartOps.get(skuId.toString());
    //如果没有就添加数据
    if (StringUtils.isEmpty(productRedisValue)) {

      //2、添加新的商品到购物车(redis)
      CartItemVo cartItemVo = new CartItemVo();
      //开启第一个异步任务
      CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
        //1、远程查询当前要添加商品的信息
        R productSkuInfo = productFeignService.getInfo(skuId);
        SkuInfoVo skuInfo = productSkuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
        });
        //数据赋值操作
        cartItemVo.setSkuId(skuInfo.getSkuId());
        cartItemVo.setTitle(skuInfo.getSkuTitle());
        cartItemVo.setImage(skuInfo.getSkuDefaultImg());
        cartItemVo.setPrice(skuInfo.getPrice());
        cartItemVo.setCount(num);
      }, executor);

      //开启第二个异步任务
      CompletableFuture<Void> getSkuAttrValuesFuture = CompletableFuture.runAsync(() -> {
        //2、远程查询skuAttrValues组合信息
        List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
        cartItemVo.setSkuAttrValues(skuSaleAttrValues);
      }, executor);

      //等待所有的异步任务全部完成
      CompletableFuture.allOf(getSkuInfoFuture, getSkuAttrValuesFuture).get();

      String cartItemJson = JSON.toJSONString(cartItemVo);
      cartOps.put(skuId.toString(), cartItemJson);

      return cartItemVo;
    } else {
      //购物车有此商品，修改数量即可
      CartItemVo cartItemVo = JSON.parseObject(productRedisValue, CartItemVo.class);
      cartItemVo.setCount(cartItemVo.getCount() + num);
      //修改redis的数据
      String cartItemJson = JSON.toJSONString(cartItemVo);
      cartOps.put(skuId.toString(), cartItemJson);

      return cartItemVo;
    }
  }


  /**
   * 获取到我们要操作的购物车
   *
   * @return
   */
  private BoundHashOperations<String, Object, Object> getCartOps() {
    //先得到当前用户信息
    UserInfoTo userInfoTo = CartInterceptor.toThreadLocal.get();

    String cartKey = "";
    if (userInfoTo.getUserId() != null) {
      //gulimall:cart:1
      cartKey = CART_PREFIX + userInfoTo.getUserId();
    } else {
      cartKey = CART_PREFIX + userInfoTo.getUserKey();
    }

    //绑定指定的key操作Redis
    BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

    return operations;
  }


}
