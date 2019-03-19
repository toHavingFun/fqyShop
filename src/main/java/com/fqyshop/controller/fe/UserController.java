package com.fqyshop.controller.fe;

import com.fqyshop.common.Const;
import com.fqyshop.common.ResponseCode;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.User;
import com.fqyshop.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CurrencyEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/1/16.
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;

    /**
     * @method Description:login
     * @parameter :user
     * @ReturnValue:the server response
     */
    @RequestMapping(value = "login.action", method = RequestMethod.POST)
    @ResponseBody()
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * @methodDescription:logout
     * @Parameter:HttpSession
     * @Return:serverResponse
     */
    @RequestMapping(value = "logout.action", method = RequestMethod.GET)
    @ResponseBody()
    public ServerResponse<User> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.response();
    }

    @RequestMapping(value = "register.action", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    @RequestMapping(value = "checkfield.action", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkField(String field, String type) {
        return iUserService.checkField(field, type);
    }

    @RequestMapping(value = "getUserInfo.action", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.responseData(user);
        }
        return ServerResponse.responseErrMsg("未登录，无法获取用户信息！");
    }

    @RequestMapping(value = "getQuestion.action", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> getQuestion(String username) {
        return iUserService.getQuestion(username);
    }

    @RequestMapping(value = "checkAnswer.action", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "forgetResetPwd.action", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPwd(String username, String newpwd, String forgetToken) {
        return iUserService.forgetResetPwd(username, newpwd, forgetToken);
    }

    @RequestMapping(value = "resetPwd.action", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPwd(HttpSession session, String oldPwd, String newPwd) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.responseErrMsg("未登录！");
        }
        return iUserService.resetPwd(oldPwd, newPwd, user);
    }

    @RequestMapping(value = "updateInformation.action", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpSession session, User user) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            ServerResponse.responseErrMsg("未登录！");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @RequestMapping(value = "getInfomation.action", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInfomation(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.responseErrCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录，需强制登录 status=10");
        }
        return iUserService.getInfomation(user.getId());
    }
}

