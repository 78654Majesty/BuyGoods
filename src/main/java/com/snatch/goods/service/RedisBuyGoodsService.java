package com.snatch.goods.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.Objects;

/**
 * @author fanglingxiao
 * @createTime 2019/1/18
 */
@Component
public class RedisBuyGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    private String buyGoodsScript=
            //将产品编号保存到集合中
            "redis.call('sadd',KEYS[1],ARGV[2]) \n"
            //购买列表
            +"local buyGoodsList = KEYS[2]..ARGV[2] \n"
            //用户编号
            +"local userId = ARGV[l] \n"
            //产品键
            +"local product ='product_'..ARGV(2] \n"
            //购买数量
            +"local count = tonumber (ARGV[3]) \n"
            //当前库存
            +"local stock= tonumber(redis.call('hget',product,'stock')) \n"
            //价格
            +"local price= tonumber(redis.call('hget',product,'price')) \n"
            //购买时间
            +"local purchase_date = ARGV[4] \n"
            //库存不足，返回0
            +"if stock < count then return 0 end \n"
            //减库存
            +"stock = stock - count \n"
            +"redis .call('hset',product,'stock',tostring(stock)) \n"
            //计算价格
            +"local sum=price * count \n"
            //合并购买记录数据
            +"local buyGoodsRecord = userId..','..count..','"
            +"..sum..','..price..','..buy_time \n"
            //将购买记录保存到list中
            +"redis.call('rpush',buyGoodsList,buyGoodsRecord) \n"
            //返回成功
            +"return 1 \n";

    /**
     *redis购买记录集合前缀
     */
    private static final String PURCHASE_PRODUCT_LIST = "purchase_list_";
    /**
     *抢购商品集合
     */
    private static final String PRODUCT_SCHEDULE_SET = "product_schedule_set";
    /**
     * 32位SHA1编码，第一次执行的时候先让redis进行缓存脚本返回
     */
    private String sha1 = null;

    public boolean buyGoodsForRedis(){

        Jedis jedis;
        try{
            jedis=(Jedis) Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().getNativeConnection();
            //如果未加载，先将脚本加载到redis服务器 返回sha1
            if (StringUtils.isEmpty(sha1)){
                sha1=jedis.scriptLoad(buyGoodsScript);
            }

            //执行脚本返回结果
            //jedis.evalsha(sha1,2,PRODUCT_SCHEDULE_SET,PURCHASE_PRODUCT_LIST,userId+"",)
        }catch (Exception e){}
        return false;
    }

}
