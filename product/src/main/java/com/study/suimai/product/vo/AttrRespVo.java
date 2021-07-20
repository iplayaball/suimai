package com.study.suimai.product.vo;

import com.study.suimai.product.entity.AttrEntity;
import lombok.Data;

@Data
public class AttrRespVo extends AttrEntity {
  private String catelogName;
  private String groupName;
}
