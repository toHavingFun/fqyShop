package com.fqyshop.controller.backend;

import com.fqyshop.common.Const;
import com.fqyshop.common.ResponseCode;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.User;
import com.fqyshop.service.IUserService;
import com.fqyshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/1/25.
 */
@RequestMapping("/admin/order")
@Controller
public class OrderManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private OrderService orderService;

    @RequestMapping("list.action")
    @ResponseBody

    public ServerResponse list(HttpSession session,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return orderService.manageList(pageNum,pageSize);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("detail.action")
    @ResponseBody
    public ServerResponse list(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return orderService.manageDetail(orderNo);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("search.action")
    @ResponseBody
    public ServerResponse orderSearch(HttpSession session,Long orderNo,
                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return orderService.manageSearch(orderNo,pageNum,pageSize);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }

    @RequestMapping("sendGoods.action")
    @ResponseBody
    public ServerResponse orderSearch(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "未登录，请登录！");
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return orderService.manageSendGoods(orderNo);
        } else {
            return ServerResponse.responseErrMsg("非管理员，无权限操作");
        }
    }
}

