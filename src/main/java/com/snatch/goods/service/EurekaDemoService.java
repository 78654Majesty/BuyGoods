package com.snatch.goods.service;

import com.snatch.goods.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author fanglingxiao
 * @date  2019/1/26
 */
@Service
public class EurekaDemoService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    private static final Logger logger = LoggerFactory.getLogger(EurekaDemoService.class);

    private static final String MALL_SERVER="mall-server";

    /**
     *
     * @param id id
     * @return User
     */
    public User getUserById(Integer id){

        String serverUrl =  getServerUrl();
        return restTemplate.getForObject(serverUrl + id, User.class);
    }

    private String getServerUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances(EurekaDemoService.MALL_SERVER);

        ServiceInstance serviceInstance = instances.get(0);
        String serverUrl = "http://"+serviceInstance.getHost()+":"+serviceInstance.getPort()+"/user/";

        logger.info("server url is {}",serverUrl);
        return serverUrl;
    }
}
