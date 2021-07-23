package com.study.suimai.product.controller;

import com.study.common.utils.PageUtils;
import com.study.common.utils.R;
import com.study.suimai.product.entity.AttrEntity;
import com.study.suimai.product.entity.AttrGroupEntity;
import com.study.suimai.product.service.AttrAttrgroupRelationService;
import com.study.suimai.product.service.AttrGroupService;
import com.study.suimai.product.service.AttrService;
import com.study.suimai.product.service.CategoryService;
import com.study.suimai.product.vo.AttrGroupRelationVo;
import com.study.suimai.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 10:56:44
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
  @Autowired
  private AttrGroupService attrGroupService;

  @Autowired
  private AttrService attrService;

  @Autowired
  private CategoryService categoryService;

  @Autowired
  AttrAttrgroupRelationService relationService;

  // 保存关联关系
  @PostMapping("/attr/relation")
  public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){

    relationService.saveBatch(vos);
    return R.ok();
  }

  // 删除关联关系
  @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody  List<AttrGroupRelationVo> vos){
    attrService.deleteRelation(vos);
    return R.ok();
  }

  @GetMapping("/{catelogId}/withattr")
  public R getAttrGroupWithAttrs(@PathVariable("catelogId")Long catelogId){

    //1、查出当前分类下的所有属性分组，
    //2、查出每个属性分组的所有属性
    List<AttrGroupWithAttrsVo> vos = attrGroupService
     .getAttrGroupWithAttrsByCatelogId(catelogId);
    return R.ok().put("data",vos);
  }

  /**
   * 列出指定分组下的基本属性
   */
  @RequestMapping("/{attrgroupId}/attr/relation")
  // @RequiresPermissions("product:attrgroup:list")
  public R list(@PathVariable Long attrgroupId) {
    List<AttrEntity> entities = attrService.getByGroupId(attrgroupId);

    return R.ok().put("data", entities);
  }

  /**
   * 列出当前分类下没有被关联的属性（可以被关联）
   */
  @GetMapping("/{attrgroupId}/noattr/relation")
  public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                          @RequestParam Map<String, Object> params){
    PageUtils page = attrService.getNoRelationAttr(params,attrgroupId);
    return R.ok().put("page",page);
  }

  /**
   * 列表
   */
  @RequestMapping("/list/{catelogId}")
  // @RequiresPermissions("product:attrgroup:list")
  public R list(@RequestParam Map<String, Object> params,
                @PathVariable Long catelogId) {
    PageUtils page = attrGroupService.queryPage(params, catelogId);

    return R.ok().put("page", page);
  }

  /**
   * 信息
   */
  @RequestMapping("/info/{attrGroupId}")
  // @RequiresPermissions("product:attrgroup:info")
  public R info(@PathVariable("attrGroupId") Long attrGroupId) {
    AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

    Long catelogId = attrGroup.getCatelogId();
    Long[] path = categoryService.findCatelogPath(catelogId);
    attrGroup.setCatelogPath(path);

    return R.ok().put("attrGroup", attrGroup);
  }

  /**
   * 保存
   */
  @RequestMapping("/save")
  // @RequiresPermissions("product:attrgroup:save")
  public R save(@RequestBody AttrGroupEntity attrGroup) {
    attrGroupService.save(attrGroup);

    return R.ok();
  }

  /**
   * 修改
   */
  @RequestMapping("/update")
  // @RequiresPermissions("product:attrgroup:update")
  public R update(@RequestBody AttrGroupEntity attrGroup) {
    attrGroupService.updateById(attrGroup);

    return R.ok();
  }

  /**
   * 删除
   */
  @RequestMapping("/delete")
  // @RequiresPermissions("product:attrgroup:delete")
  public R delete(@RequestBody Long[] attrGroupIds) {
    attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

    return R.ok();
  }

}
