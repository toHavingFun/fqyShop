package com.fqyshop.dao;

import com.fqyshop.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> getByOrderNoUserId(Long orderNo,Integer userId);
    List<OrderItem> getByOrderNo(Long orderNo);

    void batchInsert(@Param("orderItemList")List<OrderItem> orderItemList);
}