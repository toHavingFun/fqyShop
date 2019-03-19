package com.fqyshop.service.impl;

import com.fqyshop.common.Const;
import com.fqyshop.common.ResponseCode;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.dao.CartMapper;
import com.fqyshop.dao.ProductMapper;
import com.fqyshop.pojo.Cart;
import com.fqyshop.pojo.Product;
import com.fqyshop.service.ICartService;
import com.fqyshop.service.IFileService;
import com.fqyshop.util.BigDecimalUtil;
import com.fqyshop.util.PropertiesUtil;
import com.fqyshop.vo.CartProductVo;
import com.fqyshop.vo.CartVO;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.sf.jsqlparser.expression.DoubleValue;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Administrator on 2019/1/22.
 */
@Service("iCartService")
public class CartServiceimpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    public ServerResponse<CartVO> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null) {
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        } else {
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        return this.list(userId);
    }

    public ServerResponse<CartVO> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.list(userId);
    }

    public ServerResponse<CartVO> deleteProduct(Integer userId, String productIds) {
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productList)) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId, productList);
        return this.list(userId);
    }
    public ServerResponse<CartVO> list(Integer userId){
        CartVO cartVO = this.getCartVoLimit(userId);
        return ServerResponse.responseData(cartVO);
    }
    public ServerResponse<CartVO> selectOrUnSelect(Integer userId,Integer productId,Integer checked){
       cartMapper.checkedOrUnCheckedProduct(userId,productId,checked);
       return this.list(userId);
    }

    public  ServerResponse<Integer> getCartProductCount(Integer userId){
       if (userId==null){
           return ServerResponse.responseData(0);
       }
        return ServerResponse.responseData(cartMapper.selectCartProductCount(userId));
    }

    private CartVO getCartVoLimit(Integer userId) {
        CartVO cartVO = new CartVO();
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart c : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(c.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(c.getProductId());

                Product product = productMapper.selectByPrimaryKey(c.getProductId());
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProdcutSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    int buyLimitCount = 0;
                    if (product.getStock() >= c.getQuantity()) {
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                        buyLimitCount = c.getQuantity();
                    } else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(c.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKey(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(c.getChecked());
                }
                if (c.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);

            }
        }
        cartVO.setCartTotalPrice(cartTotalPrice);
        cartVO.setCartProductVoList(cartProductVoList);
        cartVO.setAllChecked(this.getAllCheckedStatus(userId));
        cartVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVO;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

}
