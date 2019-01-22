package com.snatch.goods;
import java.math.BigDecimal;

import com.snatch.goods.entity.Product;
import com.snatch.goods.service.GoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsApplicationTests {

    @MockBean
    private GoodsService goodsService;

    @Test
    public void contextLoads() {
        //构建对象
//        Product mockProduct = new Product();
//        mockProduct.setId(1);
//        mockProduct.setProductName("");
//        mockProduct.setStock(0);
//        mockProduct.setPrice(new BigDecimal("0"));
//        mockProduct.setVersion(0);
//        mockProduct.setNote("");
        //指定Mock Bean 方法 参数
        BDDMockito.given(goodsService.buyGoods(1,2,1))
                //指定mock返回对象
                .willReturn(true);
        //测试
        boolean result = goodsService.buyGoods(1, 2, 1);
    }

}

