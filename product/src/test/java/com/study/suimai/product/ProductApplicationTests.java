package com.study.suimai.product;

import com.study.suimai.product.dao.AttrGroupDao;
import com.study.suimai.product.dao.SkuSaleAttrValueDao;
import com.study.suimai.product.entity.BrandEntity;
import com.study.suimai.product.service.BrandService;
import com.study.suimai.product.vo.SkuItemSaleAttrVo;
import com.study.suimai.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ProductApplicationTests {

    @Autowired
    BrandService brandService;

  @Resource
  private AttrGroupDao attrGroupDao;

  @Resource
  private SkuSaleAttrValueDao skuSaleAttrValueDao;

  @Test
  public void test1() {
    List<SkuItemSaleAttrVo> saleAttrBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(3L);
//    saleAttrBySpuId.forEach(System.out::println);
    System.out.println(saleAttrBySpuId);
  }

  @Test
  public void test() {
    List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(3L, 225L);
    attrGroupWithAttrsBySpuId.forEach(System.out::println);
  }

    @Test
    void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setName("测试格式化sql");
		brandService.save(brandEntity);

//		brandEntity.setBrandId(2L);
//		brandEntity.setDescript("华为手机");
//		brandService.updateById(brandEntity);

        List<BrandEntity> list = brandService.list();
//        list.forEach((entity) -> {
//            System.out.println("entity = " + entity);
//        });
    }
}
