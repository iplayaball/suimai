package com.study.suimai.auth.feign;

import com.study.common.utils.R;
import com.study.suimai.auth.vo.UserLoginVo;
import com.study.suimai.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient("member")
public interface MemberFeignService {

  @PostMapping(value = "/member/member/register")
  R register(@RequestBody UserRegisterVo vo);


    @PostMapping(value = "/member/member/login")
    R login(@RequestBody UserLoginVo vo);

//    @PostMapping(value = "/member/member/oauth2/login")
//    R oauthLogin(@RequestBody SocialUser socialUser) throws Exception;
//
//    @PostMapping(value = "/member/member/weixin/login")
//    R weixinLogin(@RequestParam("accessTokenInfo") String accessTokenInfo);
}
