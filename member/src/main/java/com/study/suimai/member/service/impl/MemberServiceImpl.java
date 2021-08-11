package com.study.suimai.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.utils.PageUtils;
import com.study.common.utils.Query;
import com.study.suimai.member.dao.MemberDao;
import com.study.suimai.member.entity.MemberEntity;
import com.study.suimai.member.exception.PhoneException;
import com.study.suimai.member.exception.UsernameException;
import com.study.suimai.member.service.MemberService;
import com.study.suimai.member.vo.MemberUserLoginVo;
import com.study.suimai.member.vo.MemberUserRegisterVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

  @Override
  public MemberEntity login(MemberUserLoginVo vo) {
    String loginacct = vo.getLoginacct();
    String password = vo.getPassword();

    //1、去数据库查询 SELECT * FROM ums_member WHERE username = ? OR mobile = ?
    MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
     .eq("username", loginacct).or().eq("mobile", loginacct));

    if (memberEntity == null) {
      //登录失败
      return null;
    } else {
      //获取到数据库里的password
      String password1 = memberEntity.getPassword();
      BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      //进行密码匹配
      boolean matches = passwordEncoder.matches(password, password1);
      if (matches) {
        //登录成功
        return memberEntity;
      }
    }

    return null;
  }

  @Override
  public void register(MemberUserRegisterVo vo) {

    MemberEntity memberEntity = new MemberEntity();

    //设置默认等级
//    MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
//    memberEntity.setLevelId(levelEntity.getId());
    memberEntity.setLevelId(1L);

    //设置其它的默认信息
    //检查用户名和手机号是否唯一。感知异常，异常机制
    checkPhoneUnique(vo.getPhone());
    checkUserNameUnique(vo.getUserName());

    memberEntity.setNickname(vo.getUserName());
    memberEntity.setUsername(vo.getUserName());
    //密码进行MD5加密
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    String encode = bCryptPasswordEncoder.encode(vo.getPassword());
    memberEntity.setPassword(encode);
    memberEntity.setMobile(vo.getPhone());
    memberEntity.setGender(0);
    memberEntity.setCreateTime(new Date());

    //保存数据
    this.baseMapper.insert(memberEntity);
  }

  public void checkPhoneUnique(String phone) throws PhoneException {

    Integer phoneCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));

    if (phoneCount > 0) {
      throw new PhoneException();
    }

  }

  public void checkUserNameUnique(String userName) throws UsernameException {

    Integer usernameCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));

    if (usernameCount > 0) {
      throw new UsernameException();
    }
  }

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    IPage<MemberEntity> page = this.page(
     new Query<MemberEntity>().getPage(params),
     new QueryWrapper<MemberEntity>()
    );

    return new PageUtils(page);
  }

}