package com.snatch.goods.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.snatch.goods.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author fanglingxiao
 * @date 2019/1/16
 */

@Service
public class ConsumerService {

    private ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
    public void message(Message message, Channel channel){
        try{
            //生产者在未接收到确认消息之前，是不会给消费者发送消息！prefetchCount是一次执行的消息个数
            channel.basicQos(3);
            String value = mapper.readValue(message.getBody(), String.class);
            logger.info("rabbitmq receive msg success {}",value);
        }catch (Exception e){
            logger.error("rabbitmq receive msg error",e);
        }
    }
}

