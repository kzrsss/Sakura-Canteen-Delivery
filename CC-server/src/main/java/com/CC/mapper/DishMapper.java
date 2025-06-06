package com.CC.mapper;

import com.github.pagehelper.Page;
import com.CC.annotation.AutoFill;
import com.CC.dto.DishPageQueryDTO;
import com.CC.entity.Dish;
import com.CC.enumeration.OperationType;
import com.CC.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据主键查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(long id);

    /**
     * 根据主键删除菜品
     * @param id
     */
//    @Delete("delete from dish where id =#{id}")
//    void deleteById(Long id);

    /**
     * 根据菜品id集合批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id动态修改菜品
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 动态条件查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 起售或停售商品
     * @param id
     */
     void startOrStop(Long id);


        /**
         * 根据条件统计菜品数量
         * @param map
         * @return
         */
        Integer countByMap(Map map);
}
