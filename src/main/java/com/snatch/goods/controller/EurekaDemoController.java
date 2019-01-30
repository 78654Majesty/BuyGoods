package com.snatch.goods.controller;

import com.snatch.goods.entity.User;
import com.snatch.goods.service.EurekaDemoService;
import com.snatch.goods.util.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author fanglingxiao
 * @date 2019/1/28
 */

@RestController
@RequestMapping("/eureka")
public class EurekaDemoController {

    @Autowired
    private EurekaDemoService eurekaDemoService;

    @GetMapping("/{id}")
    @ResponseBody
    public ApiResult<User> getUser(@PathVariable("id")Integer id){
        User user = eurekaDemoService.getUserById(id);
        return getResResult(user);
    }

    private ApiResult<User> getResResult(User user) {
        ApiResult<User> apiResult = new ApiResult<>();
        apiResult.setResCode(200);
        apiResult.setResMsg("get user success!");
        apiResult.setData(user);
        return apiResult;
    }
}
