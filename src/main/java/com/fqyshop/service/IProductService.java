package com.fqyshop.service;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.Product;
import com.fqyshop.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

/**
 * Created by Administrator on 2019/1/21.
 */
public interface IProductService {
    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse setSaleStatus(Integer procuctId, Integer status);
    ServerResponse<ProductDetailVo> adminProductDetail(Integer productId);
    ServerResponse getProductList(int pageNum, int pageSize);
    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);
    ServerResponse<ProductDetailVo> getProductDetailById(Integer productId);
    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize,String orderBy);
}
