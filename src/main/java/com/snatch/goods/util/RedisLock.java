package com.snatch.goods.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Redis分布式锁（这种方式服务器时间一定要同步，否则会出问题）
 * <p>
 * 执行步骤
 * 1.setnx(lockkey,当前时间+过期超时时间)，如果返回1，则获取锁成功；如果返回0则没有获取到锁，转向2。
 * <p>
 * 2.get(lockkey)获取值oldExpireTime，并将这个value值与当前的系统时间进行比较，如果小于当前系统时间，则认为这个锁已经超时，可以允许别的请求重新获取，转向3。
 * <p>
 * 3.计算newExpireTime=当前时间+过期超时时间，然后getset(lockkey,newExpireTime)会返回当前lockkey的值currentExpireTime。
 * <p>
 * 4.判断currentExpireTime与oldExpireTime是否相等，如果相等，说明当前getset设置成功，获取到了锁。如果不相等，说明这个锁又被别的请求获取走了，那么当前请求可以直接返回失败，或者继续重试。
 * <p>
 * 5.在获取到锁之后，当前线程可以开始自己的业务处理，当处理完毕后，比较自己的处理时间和对于锁设置的超时时间，如果小于锁设置的超时时间，则直接执行delete释放锁；如果大于锁设置的超时时间，则不需要再锁进行处理。
 *
 * @authoryuhao.wangwang
 * @version1.0
 * @date2017年11月3日上午10:21:27
 */
public class RedisLock {

    /**
     * 默认请求锁的超时时间(ms毫秒)
     */
    private static final long TIME_OUT = 100;

    /**
     * 默认锁的有效时间(s)
     */
    public static final int EXPIRE = 60;

    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 锁标志对应的key
     */
    private String lockKey;
    /**
     * 锁的有效时间(s)
     */
    private int expireTime = EXPIRE;

    /**
     * 请求锁的超时时间(ms)
     */
    private long timeOut = TIME_OUT;

    /**
     * 锁的有效时间
     */
    private long expires = 0;

    /**
     * 锁标记
     */
    private volatile boolean locked = false;

    final Random random = new Random();

    /**
     * 使用默认的锁过期时间和请求锁的超时时间
     *
     * @paramredisTemplate
     * @paramlockKey锁的key（Redis的Key）
     */
    public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey) {

        if (Strings.isNullOrEmpty(lockKey)) {
            throw new IllegalArgumentException("lockKeyinvalid.");
        }
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey + "_lock";
    }

    /**
     * 使用默认的请求锁的超时时间，指定锁的过期时间
     *
     * @paramredisTemplate
     * @paramlockKey锁的key（Redis的Key）
     * @paramexpireTime锁的过期时间(单位：秒)
     */
    public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey, int expireTime) {
        this(redisTemplate, lockKey);
        this.expireTime = expireTime;
    }

    /**
     * 使用默认的锁的过期时间，指定请求锁的超时时间
     *
     * @paramredisTemplate
     * @paramlockKey锁的key（Redis的Key）
     * @paramtimeOut请求锁的超时时间(单位：毫秒)
     */
    public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey, long timeOut) {
        this(redisTemplate, lockKey);
        this.timeOut = timeOut;
    }

    /**
     * 锁的过期时间和请求锁的超时时间都是用指定的值
     *
     * @paramredisTemplate
     * @paramlockKey锁的key（Redis的Key）
     * @paramexpireTime锁的过期时间(单位：秒)
     * @paramtimeOut请求锁的超时时间(单位：毫秒)
     */
    public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey, int expireTime, long timeOut) {
        this(redisTemplate, lockKey, expireTime);
        this.timeOut = timeOut;
    }

    /**
     * @return获取锁的key
     */
    public String getLockKey() {
        return lockKey;
    }

    /**
     * 获得tryLock.
     * 实现思路:主要是使用了redis的setnx命令,缓存了锁.
     * reids缓存的key是锁的key,所有的共享,value是锁的到期时间(注意:这里把过期时间放在value了,没有时间上设置其超时时间)
     * 执行过程:
     * 1.通过setnx尝试设置某个key的值,成功(当前没有这个锁)则返回,成功获得锁
     * 2.锁已经存在则获取锁的到期时间,和当前时间比较,超时的话,则设置新的值
     *
     * @returntrueiftryLockisacquired,falseacquiretimeouted
     * @throwsInterruptedExceptionincaseofthreadinterruption
     */
    public boolean tryLock() {

        if (Strings.isNullOrEmpty(lockKey)) {
            throw new IllegalArgumentException("lockKeyinvalid.");
        }

        //请求锁超时时间，纳秒
        long timeout = timeOut * 1000000;
        //系统当前时间，纳秒
        long nowTime = System.nanoTime();

        while ((System.nanoTime() - nowTime) < timeout) {
            //分布式服务器有时差，这里给1秒的误差值
            expires = System.currentTimeMillis() + expireTime * 1000 + 1000;

            //锁到期时间
            String expiresStr = String.valueOf(expires);

            Boolean r = redisTemplate.opsForValue().setIfAbsent(lockKey, expiresStr);
            if (r != null && r) {
                locked = true;
                //设置锁的有效期，也是锁的自动释放时间，也是一个客户端在其他客户端能抢占锁之前可以执行任务的时间
                //可以防止因异常情况无法释放锁而造成死锁情况的发生
                redisTemplate.expire(lockKey, expireTime, TimeUnit.SECONDS);

                //上锁成功结束请求
                return true;
            }

            //redis里的时间
            String currentValueStr = (String) redisTemplate.opsForValue().get(lockKey);

            if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
                //判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
                //tryLockisexpired

                String oldValueStr = (String) redisTemplate.opsForValue().getAndSet(lockKey, expiresStr);
                //获取上一个锁到期时间，并设置现在的锁到期时间，
                //只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
                if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
                    //防止误删（覆盖，因为key是相同的）了他人的锁——这里达不到效果，这里值会被覆盖，但是因为什么相差了很少的时间，所以可以接受

                    //[分布式的情况下]:如过这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
                    //tryLockacquired
                    locked = true;
                    return true;
                }
            }

	/**
     *
	延迟10毫秒,这里使用随机时间可能会好一点,可以防止饥饿进程的出现,即,当同时到达多个进程,
	只会有一个进程获得锁,其他的都用同样的频率进行尝试,后面有来了一些进行,也以同样的频率申请锁,这将可能导致前面来的锁得不到满足.
	使用随机的等待时间可以一定程度上保证公平性
	*/
            try {
                Thread.sleep(50, random.nextInt(50000));
            } catch (InterruptedException e) {
                logger.error("获取分布式锁休眠被中断：", e);
            }

        }
        return locked;
    }


    /**
     * 解锁
     */
    public void unlock() {
        //只有加锁成功并且锁还有效才去释放锁
        if (locked && expires > System.currentTimeMillis()) {
            redisTemplate.delete(lockKey);
            locked = false;
        }
    }

    public static <T> T lockDistributed(RedisLock locker, Callable<T> callable) {
        checkNotNull(locker);
        checkNotNull(callable);

        try {
            if (locker.tryLock()) {
                return callable.call();
            }
            return null;
        } catch (Exception e) {
            logger.error("execute callable error", e);
            return null;
        } finally {
            locker.unlock();
        }
    }

}
