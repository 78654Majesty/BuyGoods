package com.snatch.goods.entity;

/**
 * @author fanglingxiao
 * @date 2019/1/17
 */
public class BuyProductReq {
    private int userId;
    private int productId;
    private int count;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
