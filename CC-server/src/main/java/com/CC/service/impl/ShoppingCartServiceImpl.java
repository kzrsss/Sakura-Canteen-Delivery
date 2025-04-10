package com.CC.service.impl;

import com.CC.context.BaseContext;
import com.CC.dto.ShoppingCartDTO;
import com.CC.entity.Dish;
import com.CC.entity.Setmeal;
import com.CC.entity.ShoppingCart;
import com.CC.mapper.DishMapper;
import com.CC.mapper.SetmealMapper;
import com.CC.mapper.ShoppingCartMapper;
import com.CC.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //存在 +1
        if(list!=null && list.size()>0){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);
            //update shopping_cart set number = ? and userId = ?
            shoppingCartMapper.updateNumberById(cart);
        }else {
            //不存在插入
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            //判断是菜品还是套餐
            if(dishId!=null){
                //是菜品\
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                
            }else{
                //是套餐
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    /**
     * 删除购物车一个商品
     * @param shoppingCartDTO
     */
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        shoppingCart = list.get(0);
        Integer number = shoppingCart.getNumber();

        if(number==1){
            shoppingCartMapper.deleteById(shoppingCart);
        }else{
            shoppingCart.setNumber(number-1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        }
    }
}
