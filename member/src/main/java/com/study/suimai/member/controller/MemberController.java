package com.study.suimai.member.controller;

import com.study.common.exception.BizCodeEnum;
import com.study.common.utils.PageUtils;
import com.study.common.utils.R;
import com.study.suimai.member.entity.MemberEntity;
import com.study.suimai.member.exception.PhoneException;
import com.study.suimai.member.exception.UsernameException;
import com.study.suimai.member.service.MemberService;
import com.study.suimai.member.vo.MemberUserLoginVo;
import com.study.suimai.member.vo.MemberUserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 13:42:51
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
  @Autowired
  private MemberService memberService;

  @PostMapping(value = "/login")
  public R login(@RequestBody MemberUserLoginVo vo) {

    MemberEntity memberEntity = memberService.login(vo);

    if (memberEntity != null) {
      return R.ok().setData(memberEntity);
    } else {
      return R.error(BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMessage());
    }
  }

  @PostMapping(value = "/register")
  public R register(@RequestBody MemberUserRegisterVo vo) {

    try {
      memberService.register(vo);
    } catch (PhoneException e) {
      return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
    } catch (UsernameException e) {
      return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
    }

    return R.ok();
  }

  /**
   * 列表
   */
  @RequestMapping("/list")
  // @RequiresPermissions("member:member:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = memberService.queryPage(params);

    return R.ok().put("page", page);
  }


  /**
   * 信息
   */
  @RequestMapping("/info/{id}")
  // @RequiresPermissions("member:member:info")
  public R info(@PathVariable("id") Long id) {
    MemberEntity member = memberService.getById(id);

    return R.ok().put("member", member);
  }

  /**
   * 保存
   */
  @RequestMapping("/save")
  // @RequiresPermissions("member:member:save")
  public R save(@RequestBody MemberEntity member) {
    memberService.save(member);

    return R.ok();
  }

  /**
   * 修改
   */
  @RequestMapping("/update")
  // @RequiresPermissions("member:member:update")
  public R update(@RequestBody MemberEntity member) {
    memberService.updateById(member);

    return R.ok();
  }

  /**
   * 删除
   */
  @RequestMapping("/delete")
  // @RequiresPermissions("member:member:delete")
  public R delete(@RequestBody Long[] ids) {
    memberService.removeByIds(Arrays.asList(ids));

    return R.ok();
  }

}
