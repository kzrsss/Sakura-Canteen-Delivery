package com.CC.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.CC.constant.MessageConstant;
import com.CC.constant.StatusConstant;
import com.CC.dto.DishDTO;
import com.CC.dto.DishPageQueryDTO;
import com.CC.entity.Dish;
import com.CC.entity.DishFlavor;
import com.CC.entity.Setmeal;
import com.CC.exception.DeletionNotAllowedException;
import com.CC.mapper.DishFlavorMapper;
import com.CC.mapper.DishMapper;
import com.CC.mapper.SetmealDishMapper;
import com.CC.mapper.SetmealMapper;
import com.CC.result.PageResult;
import com.CC.service.DishService;
import com.CC.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增菜品和对应的口味数据
     *@param dishDTO
     */
    @Transactional//注解，要么全成功，要么全失败，不能只成功处理菜品而不处理口味或---
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //向菜品表插入一条数据

        dishMapper.insert(dish);
        Long dishId = dish.getId();
        //向口味表插入一或多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(!flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        try {
            PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
            Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
            return new PageResult(page.getTotal(), page.getResult());
        }catch(Exception e){
            log.info("查询错误:{}",e.getMessage());
        }
        return null;
    }


    /**
     * 菜品批量删除就
     * @param ids
     * @return
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除 启售
        for(long id: ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus()== StatusConstant.ENABLE){
                //启动售卖中 不允许删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //套餐中关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(ids);
        if(setmealIds!=null && !setmealIds.isEmpty()) {
            //查询到了 不允许删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品表中菜品数据、口味数据
//        for(Long id:ids){
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
//        @delete("delete from dish where id in (?,?,?)")
        //批量删除菜品、口味
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品对应的口味数据
     * @param id
     * @return
     */
    public DishVO getByIdWithFlovr(long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //数据封装到dishVo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     *根据id修改菜品信息和口味信息
      * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        //修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        long id = dishDTO.getId();
        //修改口味表：删除原有口味数据
        dishFlavorMapper.deleteByDishId(id);
        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(!flavors.isEmpty()){
            flavors.forEach(flavor -> {
                flavor.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);

    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
        if(status==StatusConstant.DISABLE){
            //如果是停售操作，要将包含该菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(dishIds);
            if(!setmealIds.isEmpty()){
                setmealIds.forEach(setmealId->{
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                });
            }
        }


    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
