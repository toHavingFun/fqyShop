package com.fqyshop.controller.backend;

import com.fqyshop.common.Const;
import com.fqyshop.common.ResponseCode;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.Product;
import com.fqyshop.pojo.User;
import com.fqyshop.service.IFileService;
import com.fqyshop.service.IProductService;
import com.fqyshop.service.IUserService;
import com.fqyshop.util.PropertiesUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by Administrator on 2019/1/21.
 */
@Controller
@RequestMapping("/admin/product")
public class ProductManagerController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    @RequestMapping("productSave.action")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("setSaleStatus.action")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.setSaleStatus(productId, status);

        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("getDetail.action")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.adminProductDetail(productId);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("list.action")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("search.action")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iProductService.searchProduct(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("upload.action")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "uploadFile") MultipartFile file, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix");
            Map fileMap = Maps.newHashMap();
            fileMap.put("url", url);
            fileMap.put("uri", targetFileName);
            return ServerResponse.responseData(fileMap);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("richtextUpload.action")
    @ResponseBody
    public Map richtextUpload(HttpSession session, @RequestParam(value = "uploadFile") MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        Map resultMap = Maps.newHashMap();
        if (user == null) {
            resultMap.put("success",false);
            resultMap.put("msg","非管理员");
            return  resultMap;
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            if (StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败！");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            response.addHeader("Access-Control-Allow_Headers","X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作！");
            return  resultMap;
        }
    }
}
