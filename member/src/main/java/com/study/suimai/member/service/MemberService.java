package com.study.suimai.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.utils.PageUtils;
import com.study.suimai.member.entity.MemberEntity;
import com.study.suimai.member.vo.MemberUserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author wdajun
 * @email wangdajunddf@gmail.com
 * @date 2021-07-14 13:42:51
 */
public interface MemberService extends IService<MemberEntity> {

  PageUtils queryPage(Map<String, Object> params);

  void register(MemberUserRegisterVo vo);
}

