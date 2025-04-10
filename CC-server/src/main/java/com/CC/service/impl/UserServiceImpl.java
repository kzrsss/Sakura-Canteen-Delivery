package com.CC.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.CC.constant.MessageConstant;
import com.CC.dto.UserLoginDTO;
import com.CC.entity.User;
import com.CC.exception.LoginFailedException;
import com.CC.mapper.UserMapper;
import com.CC.properties.WeChatProperties;
import com.CC.service.UserService;
import com.CC.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;
    //微信服务接口地址
    public final static String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());
        //判断openid是否合法，为空抛出异常
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前id是否为新用户
        User user = userMapper.getByOpenid(openid);
        //如果新用户，自动完成注册
        if(user==null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户对象，
        return user;
    }

    /**
     * 调用微信接口获取openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        //调用微信服务器接口，获取当前用户openid
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
