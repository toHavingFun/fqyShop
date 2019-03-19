package com.fqyshop.controller.backend;

import com.fqyshop.common.Const;
import com.fqyshop.common.ResponseCode;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.User;
import com.fqyshop.service.ICategoryService;
import com.fqyshop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by Administrator on 2019/1/20.
 */
@Controller
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("addCategory.action")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("setCategoryName.action")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String cateGoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.updateCategoryname(categoryId, cateGoryName);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }
    @RequestMapping("getCategory.action")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getChildrenParallelCategory(categoryId);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("getDeepCategory.action")
    @ResponseBody
    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }
}
