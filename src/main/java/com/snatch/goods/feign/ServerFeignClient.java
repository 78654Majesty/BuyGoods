package com.snatch.goods.feign;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author fanglingxiao
 * @date 2019/2/13
 */
@org.springframework.cloud.openfeign.FeignClient("mall-server")
public interface ServerFeignClient {

    /**
     * 由于时间得问题 用JSONObject接收
     * @param id id
     * @return User
     */
    @GetMapping("/user/{id}")
    @ResponseBody
    JSONObject queryById(@PathVariable("id") int id);
}
