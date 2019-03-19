package com.fqyshop.service.impl;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.dao.ShippingMapper;
import com.fqyshop.pojo.Shipping;
import com.fqyshop.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/1/23.
 */
@Service("iShippingService")
public class ShippingServiceimpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setId(userId);
        int insert = shippingMapper.insert(shipping);
        if (insert > 0) {
            Map resultMap = Maps.newHashMap();
            resultMap.put("shippingId", shipping.getId());
            return ServerResponse.responseMsgData("地址创建成功", resultMap);
        }
        return ServerResponse.responseErrMsg("创建地址失败");
    }

    public ServerResponse<String> del(Integer userId, Integer shippingId) {
        int i = shippingMapper.deleteByShippingIdUserId(userId, shippingId);
        if (i>0){
            return ServerResponse.responseErrMsg("删除地址成功");
        }
        return ServerResponse.responseErrMsg("删除地址失败");
    }

    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setId(userId);
        int insert = shippingMapper.updateByShipping(shipping);
        if (insert > 0) {
            return ServerResponse.responseMsg("地址更新成功");
        }
        return ServerResponse.responseErrMsg("创建更新失败");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
        if (shipping==null){
            return ServerResponse.responseMsg("无法查询到该地址");
        }
        return ServerResponse.responseMsgData("查询到该地址",shipping);
    }


    public ServerResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByuserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.responseData(pageInfo);

    }
}
