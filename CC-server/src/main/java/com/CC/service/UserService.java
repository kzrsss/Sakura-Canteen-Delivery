package com.CC.service;

import com.CC.dto.UserLoginDTO;
import com.CC.entity.User;

public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
