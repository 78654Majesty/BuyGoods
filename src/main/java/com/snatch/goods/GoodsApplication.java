package com.snatch.goods;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * @author fang
 * @date 2019/1/28
 */
@MapperScan("com.snatch.goods.dao")
@SpringBootApplication
//spring定时任务
//@EnableScheduling
//异步
//@EnableAsync
@EnableDiscoveryClient
//feign
@EnableFeignClients
public class GoodsApplication {
    //@Bean
    /**
     * feign集成负载均衡
     *开启负载均衡，调用服务可以通过服务名来访问
     */
//    @LoadBalanced
//    public RestTemplate restTemplate() {
//        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
//    }

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }

}

