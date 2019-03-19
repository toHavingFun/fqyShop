package com.fqyshop.service.impl;

import com.fqyshop.common.Const;
import com.fqyshop.common.ServerResponse;
import com.fqyshop.common.TokenCache;
import com.fqyshop.dao.UserMapper;
import com.fqyshop.pojo.User;
import com.fqyshop.service.IUserService;
import com.fqyshop.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by Administrator on 2019/1/16.
 */
@Service("iUserService")
public class UserServiceimpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int i = userMapper.checkUsername(username);
        if (i == 0) {
            return ServerResponse.responseErrMsg("用户名不存在");
        }
        String md5_password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.userLogin(username, md5_password);
        if (user == null) {
            return ServerResponse.responseErrMsg("密码错误！");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.responseMsgData("登录成功", user);

    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> checkResponse = this.checkField(user.getUsername(), Const.USERNAME);
        if (!checkResponse.isSuccess()) {
            return checkResponse;
        }
        checkResponse = this.checkField(user.getEmail(), Const.EMAIL);
        if (!checkResponse.isSuccess()) {
            return checkResponse;
        }
        user.setRole(Const.Role.CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int insert = userMapper.insert(user);
        if (insert > 0) {
            return ServerResponse.responseMsg("注册成功");
        }
        return ServerResponse.responseErrMsg("注册失败");
    }

    public ServerResponse<String> checkField(String field, String type) {
        if (StringUtils.isNotBlank(field)) {
            if (Const.USERNAME.equals(type)) {
                int i = userMapper.checkUsername(field);
                if (i > 0) {
                    return ServerResponse.responseErrMsg("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int i = userMapper.checkEmail(field);
                if (i > 0) {
                    return ServerResponse.responseErrMsg("email已存在");
                }
            }
        } else {
            return ServerResponse.responseErrMsg("参数错误");
        }
        return ServerResponse.responseMsg("校验通过");
    }

    public ServerResponse<String> getQuestion(String username) {
        ServerResponse<String> checkResponse = this.checkField(username, Const.USERNAME);
        if (checkResponse.isSuccess()) {
            return ServerResponse.responseErrMsg("不存在该用户！");
        }
        String question = userMapper.getQuestionByUserName(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.responseData(question);
        }
        return ServerResponse.responseErrMsg("没有找到该问题");
    }


    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int i = userMapper.checkAnswer(username, question, answer);
        if (i > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey("token" + username, forgetToken);
            return ServerResponse.responseData(forgetToken);
        }
        return ServerResponse.responseErrMsg("问题答案错误");

    }


    @Override
    public ServerResponse<String> forgetResetPwd(String username, String newpwd, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.responseErrMsg("token不能为空！");
        }
        ServerResponse<String> checkResponse = this.checkField(username, Const.USERNAME);
        if (checkResponse.isSuccess()) {
            return ServerResponse.responseErrMsg("不存在该用户！");
        }
        String token = TokenCache.getKey("token_" + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.responseErrMsg("token无效/过期");
        }
        if (StringUtils.equals(forgetToken, token)) {
            String md5password = MD5Util.MD5EncodeUtf8(newpwd);
            int i = userMapper.updatePasswordByUsername(username, md5password);
            if (i > 0) {
                ServerResponse.responseErrMsg("修改密码成功！");
            }
        } else {
            return ServerResponse.responseErrMsg("token错误，请重新获取");
        }
        return ServerResponse.responseErrMsg("密码修改失败！");
    }

    public ServerResponse<String> resetPwd(String oldPwd, String newPwd, User user) {
        int i = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPwd), user.getId());
        if (i == 0) {
            return ServerResponse.responseErrMsg("原密码不正确！");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPwd));
        int result = userMapper.updateByPrimaryKeySelective(user);
        if (result > 0) {
            return ServerResponse.responseMsg("密码更新成功！");
        }
        return ServerResponse.responseErrMsg("密码更新失败！");

    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        int i = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (i > 0) {
            ServerResponse.responseErrMsg("email已经存在,请更换email后再次尝试");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int result = userMapper.updateByPrimaryKeySelective(updateUser);
        if (result > 0) {
            return ServerResponse.responseMsgData("更新个人信息成功", updateUser);
        }
        return ServerResponse.responseErrMsg("更新个人信息失败");
    }

    public ServerResponse<User> getInfomation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user==null){
            return ServerResponse.responseErrMsg("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.responseData(user);
    }


    // backend
    public ServerResponse checkAdminRole(User user){
        if(user!=null&& user.getRole().intValue()==Const.Role.ADMIN){
            return ServerResponse.response();
        }
        return ServerResponse.responseError();
    }
}
