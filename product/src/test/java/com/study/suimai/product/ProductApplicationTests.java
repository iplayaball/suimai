package com.study.suimai.product;

import com.study.suimai.product.entity.BrandEntity;
import com.study.suimai.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
//		BrandEntity brandEntity = new BrandEntity();
//		brandEntity.setName("华为");
//		brandService.save(brandEntity);

//		brandEntity.setBrandId(2L);
//		brandEntity.setDescript("华为手机");
//		brandService.updateById(brandEntity);

        List<BrandEntity> list = brandService.list();
        list.forEach((entity) -> {
            System.out.println("entity = " + entity);
        });
    }
}
