package com.fqyshop.service;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.vo.CartVO;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2019/1/22.
 */

public interface ICartService {
    ServerResponse<CartVO> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVO> update(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVO> deleteProduct(Integer userId, String productIds);
    ServerResponse<CartVO> list(Integer userId);
    ServerResponse<CartVO> selectOrUnSelect(Integer userId,Integer productId,Integer checked);
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
