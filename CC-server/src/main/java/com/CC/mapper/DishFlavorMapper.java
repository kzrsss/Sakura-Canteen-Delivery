package com.CC.mapper;

import com.CC.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * @param flavors
     */

    void insertBatch(List<DishFlavor> flavors);

    /**
     * 删除口味
     * @param DishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dissid}")
    void deleteByDishId(Long DishId);

    /**
     * 根据菜品id集合批量删除
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品id查询菜品对应的口味数据
     * @param DishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id=#{DishId}")
    List<DishFlavor> getByDishId(long DishId);
}
