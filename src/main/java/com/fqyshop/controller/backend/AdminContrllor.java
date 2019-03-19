package com.fqyshop.controller.backend;

import com.fqyshop.common.Const;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.User;
import com.fqyshop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

/**
 * Created by Administrator on 2019/1/20.
 */
@Controller
@RequestMapping("/admin/admin")
public class AdminContrllor {
    @Autowired
    private IUserService iUserService;
    @RequestMapping(value = "login.action",method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()){
            User responseData = response.getData();
            if(responseData.getRole()== Const.Role.ADMIN){
                session.setAttribute(Const.CURRENT_USER,responseData);
                return response;
            }else {
                return ServerResponse.responseErrMsg("非管理员，无法登录！");
            }
        }
        return response;
    }
}
