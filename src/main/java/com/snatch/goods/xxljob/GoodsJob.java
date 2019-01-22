package com.snatch.goods.xxljob;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import org.springframework.stereotype.Component;

/**
 * @author fanglingxiao
 * @date 2019/1/21
 */
@Component
@JobHandler(value = "GoodsJob")
public class GoodsJob extends IJobHandler {

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        //...
        return ReturnT.SUCCESS;
    }
}
