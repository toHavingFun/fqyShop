package com.fqyshop.service;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.vo.OrderVo;
import com.github.pagehelper.PageInfo;

import java.util.Map;

/**
 * Created by Administrator on 2019/1/25.
 */
public interface OrderService {
    ServerResponse pay(Long orderNo, Integer userId, String path);

    ServerResponse aliCallback(Map<String, String> params);

    ServerResponse queryOderPayStatus(Integer userId, Long orderNo);

    ServerResponse createOrder(Integer userId, Integer shippingId);

    ServerResponse<String> cancel(Integer userId, Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);

    ServerResponse<PageInfo> getOrderList(Integer userId, Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    ServerResponse<OrderVo> manageDetail(Long orderNo);

    ServerResponse<PageInfo> manageSearch(Long orderNo, Integer pageNum, Integer pageSize);

    ServerResponse<String> manageSendGoods(Long orderNo);
}
