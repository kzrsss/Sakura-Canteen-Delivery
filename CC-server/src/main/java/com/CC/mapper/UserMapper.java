package com.CC.mapper;

import com.CC.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{userId} ")
    User getById(Long userId);

    /**
     * 新用户数量查询
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
