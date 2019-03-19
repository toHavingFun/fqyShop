package com.fqyshop.dao;

import com.fqyshop.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    User userLogin(@Param("username") String username, @Param("password")String password);

    int checkEmail(@Param("email")String email);

    String getQuestionByUserName(String username);

    int checkAnswer(@Param("username") String username, @Param("question")String question, @Param("answer")String answer);

    int updatePasswordByUsername(@Param("username")String username, @Param("md5password")String md5password);

    int checkPassword(@Param("password") String password,@Param("userId") Integer userId);

    int checkEmailByUserId(@Param("email") String email,@Param("userId") Integer userId);
}