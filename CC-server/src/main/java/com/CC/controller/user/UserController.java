package com.CC.controller.user;

import com.CC.constant.JwtClaimsConstant;
import com.CC.dto.UserLoginDTO;
import com.CC.entity.User;
import com.CC.properties.JwtProperties;
import com.CC.result.Result;
import com.CC.service.UserService;
import com.CC.utils.JwtUtil;
import com.CC.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags="C端用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;


    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping({"/login"})
    @ApiOperation("微信登录")
    public Result<UserLoginVO> Login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信用户登录:{}",userLoginDTO.getCode());

        //微信登陆
        User user = userService.wxLogin(userLoginDTO);

        //为微信用户生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),claims);

        UserLoginVO userLoginVo = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVo);
    }
}
