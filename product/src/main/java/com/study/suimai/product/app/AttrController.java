package com.study.suimai.product.app;

import com.study.common.utils.PageUtils;
import com.study.common.utils.R;
import com.study.suimai.product.service.AttrService;
import com.study.suimai.product.vo.AttrRespVo;
import com.study.suimai.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 商品属性
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
  @Autowired
  private AttrService attrService;

  /**
   * 列表
   */
  @RequestMapping("/{attrType}/list/{catelogId}")
  // @RequiresPermissions("product:attr:list")
  public R listBase(@RequestParam Map<String, Object> params,
                    @PathVariable("attrType") String attrType,
                    @PathVariable("catelogId") Long catelogId) {
    PageUtils page = attrService.queryPage(params, attrType, catelogId);

    return R.ok().put("page", page);
  }

  /**
   * 列表
   */
  @RequestMapping("/list")
  // @RequiresPermissions("product:attr:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = attrService.queryPage(params);

    return R.ok().put("page", page);
  }


  /**
   * 信息
   */
  @RequestMapping("/info/{attrId}")
  // @RequiresPermissions("product:attr:info")
  public R info(@PathVariable("attrId") Long attrId) {
    AttrRespVo attrRespVo = attrService.getDetailById(attrId);

    return R.ok().put("attr", attrRespVo);
  }

  /**
   * 保存
   */
  @RequestMapping("/save")
  // @RequiresPermissions("product:attr:save")
  public R save(@RequestBody AttrVo attr) {
    attrService.saveAttrVo(attr);

    return R.ok();
  }

  /**
   * 修改
   */
  @RequestMapping("/update")
  // @RequiresPermissions("product:attr:update")
  public R update(@RequestBody AttrVo attrvo) {
    attrService.updateCascade(attrvo);

    return R.ok();
  }

  /**
   * 删除
   */
  @RequestMapping("/delete")
  // @RequiresPermissions("product:attr:delete")
  public R delete(@RequestBody Long[] attrIds) {
    attrService.removeByIds(Arrays.asList(attrIds));

    return R.ok();
  }

}
