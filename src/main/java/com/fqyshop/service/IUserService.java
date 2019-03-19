package com.fqyshop.service;

import com.fqyshop.common.ServerResponse;
import com.fqyshop.pojo.User;

/**
 * Created by Administrator on 2019/1/16.
 */
public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkField(String field, String type);
    ServerResponse<String> getQuestion(String username);
    ServerResponse<String> checkAnswer(String username, String question, String answer);
    ServerResponse<String> forgetResetPwd(String username, String newpwd, String forgetToken);
    ServerResponse<String> resetPwd(String oldPwd, String newPwd, User user);

    ServerResponse<User> updateInformation(User currentUser);
    ServerResponse<User> getInfomation(Integer userId);
    ServerResponse checkAdminRole(User user);

}
