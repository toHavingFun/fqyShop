package com.fqyshop.service;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.Shipping;
import com.github.pagehelper.PageInfo;

/**
 * Created by Administrator on 2019/1/23.
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);
    ServerResponse del(Integer userId, Integer shippingId);
    ServerResponse update(Integer userId, Shipping shipping);
    ServerResponse<Shipping> select(Integer userId, Integer shippingId);
    ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize);
}
