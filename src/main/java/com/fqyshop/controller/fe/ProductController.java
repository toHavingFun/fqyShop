package com.fqyshop.controller.fe;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.service.IProductService;
import com.fqyshop.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2019/1/22.
 */

@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private IProductService iProductService;

    @RequestMapping("detail.action")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return iProductService.getProductDetailById(productId);

    }
    @RequestMapping("list.action")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword",required = false) String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1")int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue ="5")int pageSize,
                                         @RequestParam(value = "orderBy",defaultValue ="")String orderBy) {
        return iProductService.getProductByKeywordCategory(keyword,categoryId,pageNum,pageSize,orderBy);

    }

}
