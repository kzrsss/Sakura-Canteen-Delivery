package com.CC.controller.user;

import com.CC.constant.StatusConstant;
import com.CC.entity.Dish;
import com.CC.result.Result;
import com.CC.service.DishService;
import com.CC.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        //构造redis中的key dish_id
        String key = "dish_" + categoryId;

        //查询redis中是否有数据
        Instant start = Instant.now(); // 开始时间

        List<DishVO> list  = (List<DishVO>) redisTemplate.opsForValue().get(key);

        Instant end = Instant.now(); // 结束时间

        // 计算查询时间
        long duration = java.time.Duration.between(start, end).toMillis();
        System.out.println("查询耗时: " + duration + "ms");
        if(list!=null&&list.size()>0){
            //存在，直接返回
            return Result.success(list);
        }

        //不存在，查询后插入redis
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        //从数据库中查询
        list = dishService.listWithFlavor(dish);
        //查出的数据存入redis
        redisTemplate.opsForValue().set(key,list);
        return Result.success(list);
    }

}
