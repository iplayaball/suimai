package com.study.suimai.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.study.common.constant.AuthServerConstant;
import com.study.common.exception.BizCodeEnum;
import com.study.common.utils.R;
import com.study.common.vo.MemberResponseVo;
import com.study.suimai.auth.feign.MemberFeignService;
import com.study.suimai.auth.feign.ThirdPartFeignService;
import com.study.suimai.auth.vo.UserLoginVo;
import com.study.suimai.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.study.common.constant.AuthServerConstant.LOGIN_USER;


@Slf4j
@Controller
public class LoginController {

  @Autowired
  private ThirdPartFeignService thirdPartFeignService;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  MemberFeignService memberFeignService;


  @ResponseBody
  @GetMapping(value = "/sms/sendCode")
  public R sendCodede(@RequestParam("phone") String phone) {

    //1、接口防刷
    String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
    if (!StringUtils.isEmpty(redisCode)) {
      //活动存入redis的时间，用当前时间减去存入redis的时间，判断用户手机号是否在60s内发送验证码
      long currentTime = Long.parseLong(redisCode.split("_")[1]);
      if (System.currentTimeMillis() - currentTime < 60000) {
        //60s内不能再发
        return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
      }
    }

    //2、验证码的再次效验 redis.存key-phone,value-code
    int code = (int) ((Math.random() * 9 + 1) * 100000);
    String codeNum = String.valueOf(code);
    String redisStorage = codeNum + "_" + System.currentTimeMillis();

    //存入redis，防止同一个手机号在60秒内再次发送验证码
    stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
     redisStorage, 10, TimeUnit.MINUTES);

    thirdPartFeignService.sendCode(phone, codeNum);
//    System.out.printf("%s:%s", phone, code);
    System.out.println();

    return R.ok();
  }


  /**
   * TODO: 重定向携带数据：利用session原理，将数据放在session中。
   * TODO:只要跳转到下一个页面取出这个数据以后，session里面的数据就会删掉
   * TODO：分布下session问题
   * RedirectAttributes：重定向也可以保留数据，不会丢失
   * 用户注册
   *
   * @return
   */
  @PostMapping(value = "/register")
  public String register(@Valid UserRegisterVo vos, BindingResult result,
                         Model model) {
//                         RedirectAttributes attributes) {

    String url;
//    url = request.getScheme() + "://"
//     + request.getServerName() + ":"
//     + request.getServerPort()
//     + request.getServletPath();
//    url = request.getScheme() + "://"
//     + request.getServerName()
//     + "/auth/";
//    url = "https://8089-cs-629891519430-default.cs-asia-east1-jnrc.cloudshell.dev"
//     + "/auth/";
    url = "http://127.0.0.1"
     + "/auth/";

    String redirectRegUrl = "redirect:" + url + "reg.html";
    String redirectLoginUrl = "redirect:" + url + "login.html";

    System.out.println(redirectRegUrl);

    //如果有错误回到注册页面
    if (result.hasErrors()) {
      /*Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors
       .toMap(FieldError::getField, FieldError::getDefaultMessage));
       会存在同一个字段，多个错误
       */
      Map<String, String> errors = new HashMap<>();
      result.getFieldErrors().forEach(fieldError -> {
        errors.put(fieldError.getField(), fieldError.getDefaultMessage());
      });
//      attributes.addFlashAttribute("errors", errors);
      model.addAttribute("errors", errors);
      log.info(String.valueOf(result.getFieldErrors()));

      //效验出错回到注册页面
      return "reg";
//      return "redirect:http://127.0.0.1/auth/reg.html";
    }

    //1、校验验证码
    String code = vos.getCode();

    //获取存入Redis里的验证码
    String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone());

    if (!StringUtils.isEmpty(redisCode)) {
      //截取字符串
      if (code.equals(redisCode.split("_")[0])) { //判断页面填写的code和redis存的code
        //删除验证码;令牌机制
        stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vos.getPhone());
        //验证码通过，真正注册，调用远程服务进行注册
        R register = memberFeignService.register(vos);
        if (register.getCode() == 0) {
          //成功
          return "login";
        } else {
          //失败
          Map<String, String> errors = new HashMap<>();
          log.info(register.getData("msg", new TypeReference<String>() {
          }));
          errors.put("msg", register.getData("msg", new TypeReference<String>() {
          }));
          model.addAttribute("errors", errors);
          return "reg";
        }


      } else {
        //效验出错回到注册页面
        Map<String, String> errors = new HashMap<>();
        log.info("验证码错误");
        log.info("code error");
        errors.put("code", "验证码错误");
        model.addAttribute("errors", errors);
        return "reg";
      }
    } else {
      //效验出错回到注册页面
      Map<String, String> errors = new HashMap<>();
      log.info("验证码不存在");
      log.info("code not ex");
      errors.put("code", "验证码不存在");
      model.addAttribute("errors", errors);
      return "reg";
    }
  }

  /*
      @GetMapping(value = "/login.html")
      public String loginPage(HttpSession session) {

          //从session先取出来用户的信息，判断用户是否已经登录过了
          Object attribute = session.getAttribute(LOGIN_USER);
          //如果用户没登录那就跳转到登录页面
          if (attribute == null) {
              return "login";
          } else {
              return "redirect:http://gulimall.com";
          }

      }

  */
  /*@PostMapping(value = "/login")
  public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) {
    //远程登录
    R login = memberFeignService.login(vo);

    if (login.getCode() == 0) {
      *//*MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {
      });
      session.setAttribute(LOGIN_USER, data);*//*
      return "redirect:https://8089-cs-252558529935-default.cs-asia-east1-jnrc.cloudshell.dev/";
    } else {
      Map<String, String> errors = new HashMap<>();
      errors.put("msg", login.getData("msg", new TypeReference<String>() {
      }));
      attributes.addFlashAttribute("errors", errors);
      return "redirect:https://8089-cs-252558529935-default.cs-asia-east1-jnrc.cloudshell.dev/auth/login.html";
    }
  }*/

  @ResponseBody
  @PostMapping(value = "/login")
  public R login(@RequestBody  UserLoginVo vo, HttpSession session) {
    //远程登录
    R login = memberFeignService.login(vo);

    if (login.getCode() == 0) {
      MemberResponseVo data = login.getData("data", new TypeReference<MemberResponseVo>() {
      });
      session.setAttribute(LOGIN_USER, data);
      return R.ok();
    } else {
      return R.error().put("msg", login.getData("msg", new TypeReference<String>() {}));
    }
  }

/*
    @GetMapping(value = "/loguot.html")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute(LOGIN_USER);
        request.getSession().invalidate();
        return "redirect:http://gulimall.com";
    }*/

}
