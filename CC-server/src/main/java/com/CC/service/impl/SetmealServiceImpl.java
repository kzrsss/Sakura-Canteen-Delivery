package com.CC.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.CC.constant.MessageConstant;
import com.CC.constant.StatusConstant;
import com.CC.dto.SetmealDTO;
import com.CC.dto.SetmealPageQueryDTO;
import com.CC.entity.Setmeal;
import com.CC.entity.SetmealDish;
import com.CC.exception.DeletionNotAllowedException;
import com.CC.mapper.SetmealDishMapper;
import com.CC.mapper.SetmealMapper;
import com.CC.result.PageResult;
import com.CC.service.SetmealService;
import com.CC.vo.DishItemVO;
import com.CC.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //向套餐表插入数据
        setmealMapper.insert(setmeal);

        //获取生成的套餐id
        Long id = setmeal.getId();

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setId(id);
        });
        //保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum,pageSize);

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        //判断当前套餐是否能够删除 启售
        for(long id: ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE){
                //启动售卖中 不允许删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
    ids.forEach(id->{
        //删除套餐表中的数据
        setmealMapper.deleteByIds(id);
        //删除套餐菜品关系表中的数据
        setmealDishMapper.deleteBySetmealId(id);
    });

    }

    /**
     * 启用或禁用套餐
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 根据id查询套餐和其对应的菜品数据
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id) {
        //根据套餐id，获取套餐的数据
        Setmeal setmeal = setmealMapper.getById(id);
        //根据套餐id，获取套餐内关联菜品的数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }


    /**
     * 修改套餐
     * @param setmealDTO
     */
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //1、修改套餐表，执行update
        setmealMapper.update(setmeal);

        //套餐id
        Long setmealId = setmeal.getId();

        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        setmealDishMapper.deleteBySetmealId(setmealId);

        //3.1、获取dto中新增菜品的列表存入setmealDishes
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setId(setmealId);
        });
        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
