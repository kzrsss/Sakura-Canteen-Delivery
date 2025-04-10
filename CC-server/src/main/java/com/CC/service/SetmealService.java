package com.CC.service;

import com.CC.annotation.AutoFill;
import com.CC.dto.SetmealDTO;
import com.CC.dto.SetmealPageQueryDTO;
import com.CC.entity.Setmeal;
import com.CC.enumeration.OperationType;
import com.CC.result.PageResult;
import com.CC.vo.DishItemVO;
import com.CC.vo.SetmealVO;

import java.util.List;

public interface SetmealService {


    /**
     * 新增菜品，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 启用或禁用套餐
     * @param status
     * @param id
     */
    @AutoFill(OperationType.UPDATE)
    void startOrStop(Integer status, long id);

    /**
     * 根据id查询套餐和对应的菜品数据
     *
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
