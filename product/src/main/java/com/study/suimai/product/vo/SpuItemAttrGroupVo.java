package com.study.suimai.product.vo;

import com.study.suimai.product.vo.spusave.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ToString
public class SpuItemAttrGroupVo {

    private String groupName;

    private List<Attr> attrs;

}
