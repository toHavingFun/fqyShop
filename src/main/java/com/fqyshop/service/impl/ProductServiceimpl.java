package com.fqyshop.service.impl;

import com.fqyshop.common.Const;
import com.fqyshop.common.ResponseCode;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.dao.CategoryMapper;
import com.fqyshop.dao.ProductMapper;
import com.fqyshop.pojo.Category;
import com.fqyshop.pojo.Product;
import com.fqyshop.service.ICategoryService;
import com.fqyshop.service.IProductService;
import com.fqyshop.util.DateTimeUtil;
import com.fqyshop.util.PropertiesUtil;
import com.fqyshop.vo.ProductDetailVo;
import com.fqyshop.vo.ProductListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/1/21.
 */
@Service("iProductService")
public class ProductServiceimpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId() != null) {
                int i = productMapper.updateByPrimaryKey(product);
                if (i > 0) return ServerResponse.responseMsg("更新产品成功！");
                else return ServerResponse.responseErrMsg("更新产品成功！");
            } else {
                int insert = productMapper.insert(product);
                if (insert > 0) return ServerResponse.responseMsg("新增产品成功！");
                else return ServerResponse.responseErrMsg("新增产品失败!");
            }
        }
        return ServerResponse.responseErrMsg("新增或更新产品的参数不正确");
    }

    public ServerResponse setSaleStatus(Integer procuctId, Integer status) {
        if (procuctId == null || status == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(procuctId);
        product.setStatus(status);
        int i = productMapper.updateByPrimaryKeySelective(product);
        if (i > 0) {
            return ServerResponse.responseMsg("修改产品销售状态成功");
        }
        return ServerResponse.responseMsg("修改产品销售状态成功");
    }

    public ServerResponse<ProductDetailVo> adminProductDetail(Integer productId) {
        if (productId == null)
            return ServerResponse.responseErrCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.responseErrMsg("产品已下架");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.responseData(productDetailVo);
    }


    public ServerResponse getProductList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> products = productMapper.selectList();
        List<ProductListVo> productListVos = new ArrayList<ProductListVo>();
        for (Product product : products) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(products);
        pageInfo.setList(productListVos);
        return ServerResponse.responseData(pageInfo);
    }

    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> products = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVos = new ArrayList<ProductListVo>();
        for (Product product : products) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(products);
        pageInfo.setList(productListVos);
        return ServerResponse.responseData(pageInfo);
    }

    public ServerResponse<ProductDetailVo> getProductDetailById(Integer productId) {
        if (productId == null)
            return ServerResponse.responseErrCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.responseErrMsg("产品已下架");
        }
        if (product.getStatus() != Const.ProductStatus.ON_SAlE.getCode()) {
            return ServerResponse.responseErrMsg("产品已下架");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.responseData(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize,String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            ServerResponse.responseErrMsg("查找参数错误");
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVos = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVos);
                return ServerResponse.responseData(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if (StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderBys = orderBy.split("_");
                PageHelper.orderBy(orderBys[0]+" "+orderBys[1]);
            }
        }

        List<Product> productList = productMapper.selectByNameAndCategoryIds(
                StringUtils.isBlank(keyword)?null:keyword
                ,categoryIdList);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for(Product p: productList){
            ProductListVo productListVo = assembleProductListVo(p);
            productListVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVos);
        return ServerResponse.responseData(pageInfo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.fqy12138.cn/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.fqy12138.cn/"));
        return productListVo;
    }
}

