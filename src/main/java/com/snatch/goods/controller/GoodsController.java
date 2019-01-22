package com.snatch.goods.controller;

import com.snatch.goods.service.GoodsService;
import com.snatch.goods.util.ApiResult;
import com.snatch.goods.entity.BuyProductReq;
import com.snatch.goods.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author fanglingxiao
 * @createTime 2019/1/16
 */
@Controller
@RequestMapping("/goods")
@CrossOrigin
public class GoodsController {

    @Autowired
    private ProducerService producerService;

    @Autowired
    private GoodsService goodsService;

    @RequestMapping("test")
    public String test(){
        //producerService.send();
        return "buyproduct";
    }

    @PostMapping("buy")
    @ResponseBody
    public ApiResult<String> buy(@RequestBody BuyProductReq req){
        boolean result = goodsService.buyGoods(req.getUserId(), req.getProductId(), req.getCount());
        ApiResult<String> res = new ApiResult<>();
        if (result){
            res.setResCode(200);
            res.setData(ApiResult.getSUCCESS());
            return res;
        }
        res.setResCode(620);
        res.setData(ApiResult.getFAIL());
        return res;
    }
}
