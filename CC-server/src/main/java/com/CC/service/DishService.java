package com.CC.service;

import com.CC.dto.DishDTO;
import com.CC.dto.DishPageQueryDTO;
import com.CC.entity.Dish;
import com.CC.result.PageResult;
import com.CC.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味数据
     *@param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);



    /**
     * 菜品批量删除就
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);


    /**
     * 根据id查询菜品对应的口味数据
     * @param id
     * @return
     */
    DishVO getByIdWithFlovr(long id);

    /**
     * 更新菜品信息和口味信息
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> list(long categoryId);

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
