package com.snatch.goods.dao;

import com.snatch.goods.entity.ProductBuyRecord;

public interface ProductBuyRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProductBuyRecord record);

    int insertSelective(ProductBuyRecord record);

    ProductBuyRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProductBuyRecord record);

    int updateByPrimaryKey(ProductBuyRecord record);
}