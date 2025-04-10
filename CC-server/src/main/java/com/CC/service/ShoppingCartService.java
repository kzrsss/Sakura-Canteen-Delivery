package com.CC.service;

import com.CC.dto.ShoppingCartDTO;


public interface ShoppingCartService {


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 删除购物车一个商品
     * @param id
     */
    void sub(ShoppingCartDTO id);
}
