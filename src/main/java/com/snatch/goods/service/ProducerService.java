package com.snatch.goods.service;
import java.math.BigDecimal;

import com.alibaba.fastjson.JSONObject;
import com.snatch.goods.config.RabbitMqConfig;
import com.snatch.goods.dao.ProductMapper;
import com.snatch.goods.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author fanglingxiao
 * @createTime 2019/1/16
 */
@Service
public class ProducerService implements RabbitTemplate.ConfirmCallback {

    private static final Logger logger = LoggerFactory.getLogger(ProducerService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ProductMapper productMapper;

    public void send(){
        Product product = new Product();
        product.setProductName("Spring实战");
        product.setStock(10000);
        product.setPrice(new BigDecimal("100"));
        product.setVersion(1);
        product.setNote("畅销书籍");

        int insert = productMapper.insert(product);
        sendRabbitMqMsg(String.valueOf(insert));
    }

    private void sendRabbitMqMsg(Object msg){
        String jsonString = JSONObject.toJSONString(msg);
        logger.info("rabbitmq send msg {}",msg);

        //回调必须设置
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME,RabbitMqConfig.ROUTING_KEY,jsonString);

    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack){
            logger.info("消息消费成功");
        }else {
            logger.info("消息消费失败:"+cause);
        }
    }
}
