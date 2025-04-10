package com.CC.controller.user;

import com.CC.context.BaseContext;
import com.CC.dto.ShoppingCartDTO;
import com.CC.entity.ShoppingCart;
import com.CC.mapper.ShoppingCartMapper;
import com.CC.result.Result;
import com.CC.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags="C端购物车相关接口")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车，商品：{}",shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);

        return Result.success();
    }

    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return Result.success(list);
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clearCart(){
        shoppingCartMapper.clean(BaseContext.getCurrentId());
        return Result.success();
    }

    @ApiOperation("减少购物车一个商品")
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
        }

}
