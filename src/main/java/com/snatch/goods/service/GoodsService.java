package com.snatch.goods.service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

import com.snatch.goods.dao.ProductBuyRecordMapper;
import com.snatch.goods.dao.ProductMapper;
import com.snatch.goods.entity.Product;
import com.snatch.goods.entity.ProductBuyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author fanglingxiao
 * @date 2019/1/17
 */

@Service
public class GoodsService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductBuyRecordMapper recordMapper;

    private static final Logger logger = LoggerFactory.getLogger(GoodsService.class);

    /**
     *数据库的事务修改为读写提交，乐观锁使用
     */
    @Transactional(rollbackFor = Exception.class,isolation = Isolation.READ_COMMITTED)
    public boolean buyGoods(int userId,int productId,int count){

        //或许当前系统时间
        long start = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();

        /**
         * 悲观锁：
         *  在select检查库存的时候加上 for update 作为数据库锁
         *
         *由于乐观锁导致大量请求被拒，没有购买成功
         * 解决方案：
         *  通过时间戳 每个请求给与100ms的请求时间，如果被拒继续请求
         *
         *  或者可以采用限制次数的方式 每个请求三次请求访问
         */
        while(true){

            //获取当前毫秒值,每次请求
            long end = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
            if(end-start>100){
                return false;
            }

            //获取商品信息
            Product product = productMapper.selectByPrimaryKey(productId);

            logger.info("use productId {} get Product {} ",productId,product);

            if (null == product){
                return false;
            }

            //检查库存
            Integer stock = product.getStock();

            logger.info("product stock is {} and need buy count is {} version is {}",stock,count,product.getVersion());

            if (count>stock){
                logger.info("库存不足！");
                return false;
            }

            //使用乐观锁解决超卖情况(sql设置版本)
            //扣减库存
            product.setStock(stock-count);
            int flat = productMapper.catProductForUpdate(product);

            if (flat==0){
                //版本不对 未作更新，继续请求
                continue;
            }

            //初始化购买记录
            ProductBuyRecord record = initProductBuyRecord(product,userId,count);

            //插入购买记录
            recordMapper.insert(record);
            logger.info("抢购成功！当前用户 {} ",userId);

            return true;
        }

    }

    private ProductBuyRecord initProductBuyRecord(Product product, int userId, int count) {
        ProductBuyRecord record = new ProductBuyRecord();
        record.setUserId(userId);
        record.setProductId(product.getId());

        BigDecimal price = product.getPrice();
        record.setPrice(price);
        record.setCount(count);

        BigDecimal decimal = new BigDecimal(Integer.toString(count));
        BigDecimal multiplySum = price.multiply(decimal);
        record.setSumPrice(multiplySum);

        record.setBuyTime(new Date());
        record.setNote("这本书真的好！");

        return record;
    }

    /**
     * 每天凌晨一点执行 定时任务调度执行
     */
//    @Scheduled(cron = "0 0 1 * * ?")
    public void test(){

    }
}
