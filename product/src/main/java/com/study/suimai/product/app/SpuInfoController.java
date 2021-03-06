package com.study.suimai.product.app;

import com.study.common.utils.PageUtils;
import com.study.common.utils.R;
import com.study.suimai.product.entity.SpuInfoEntity;
import com.study.suimai.product.service.SpuInfoService;
import com.study.suimai.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * spu信息
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
  @Autowired
  private SpuInfoService spuInfoService;

  //商品上架
  @PostMapping(value = "/{spuId}/up")
  public R spuUp(@PathVariable("spuId") Long spuId) {

    spuInfoService.up(spuId);

    return R.ok();
  }

  /**
   * 保存
   */
  @RequestMapping("/save")
  // @RequiresPermissions("product:spuinfo:save")
  public R save(@RequestBody SpuSaveVo vo) {
    spuInfoService.saveSpuInfo(vo);

    return R.ok();
  }

  /**
   * 列表
   */
  @RequestMapping("/list")
  // @RequiresPermissions("product:spuinfo:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = spuInfoService.queryPageByCondition(params);

    return R.ok().put("page", page);
  }


  /**
   * 信息
   */
  @RequestMapping("/info/{id}")
  // @RequiresPermissions("product:spuinfo:info")
  public R info(@PathVariable("id") Long id) {
    SpuInfoEntity spuInfo = spuInfoService.getById(id);

    return R.ok().put("spuInfo", spuInfo);
  }

  /**
   * 修改
   */
  @RequestMapping("/update")
  // @RequiresPermissions("product:spuinfo:update")
  public R update(@RequestBody SpuInfoEntity spuInfo) {
    spuInfoService.updateById(spuInfo);

    return R.ok();
  }

  /**
   * 删除
   */
  @RequestMapping("/delete")
  // @RequiresPermissions("product:spuinfo:delete")
  public R delete(@RequestBody Long[] ids) {
    spuInfoService.removeByIds(Arrays.asList(ids));

    return R.ok();
  }

}
