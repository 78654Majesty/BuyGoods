package com.snatch.goods.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author : fangligxiao
 * Explain:Redis分布式锁
 */
@Component
public class RedisLockDemo {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 加锁
     * @param key 锁唯一标志
     * @param value 超时时间+当前时间
     * @return
     */
    public boolean lock(String key, String value){

        if(stringRedisTemplate.opsForValue().setIfAbsent(key,value)) {
            return true;
        }
        //判断锁超时,防止死锁
        String currentValue = stringRedisTemplate.opsForValue().get(key);
        //如果锁过期
        if(!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()){
            //获取上一个锁的时间value
            String oldValue = stringRedisTemplate.opsForValue().getAndSet(key,value);
            if(!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue) ){
                //校验是不是上个对应的商品时间戳,也是防止并发
                return true;
            }
        }
        return false;
    }
    /**
     * 解锁
     * @param key key
     * @param value value
     */
    public void unlock(String key,String value){
        try {
            String currentValue = stringRedisTemplate.opsForValue().get(key);
            if(!StringUtils.isEmpty(currentValue) && currentValue.equals(value) ){
                //删除key
                stringRedisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (NullPointerException e) {
            System.out.println(key+":"+"[Redis分布式锁] 解锁出现异常了"+e);
        }
    }


}
