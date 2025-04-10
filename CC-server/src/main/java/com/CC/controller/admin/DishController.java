package com.CC.controller.admin;

import com.CC.dto.DishDTO;
import com.CC.dto.DishPageQueryDTO;
import com.CC.entity.Dish;
import com.CC.mapper.DishMapper;
import com.CC.result.PageResult;
import com.CC.result.Result;
import com.CC.service.DishService;
import com.CC.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 彩屏管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags="菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品;{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //清理缓存数据
        clearCache("dish_" + dishDTO.getCategoryId());
//        String key = "dish_" + dishDTO.getCategoryId();
//        redisTemplate.delete(key);

        return Result.success();
    }

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 菜品批量删除就
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除:{}",ids);
        dishService.deleteBatch(ids);


        //清理redis缓存，所有
        clearCache("dish_*");
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable long id){
        log.info("根据id查询菜品:{}",id);
        DishVO dishVo = dishService.getByIdWithFlovr(id);
        return Result.success(dishVo);

    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        //清理redis缓存，所有
        clearCache("dish_*");
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);



        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售或停售商品")
    public Result startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status,id);


        //清理redis缓存，所有
        clearCache("dish_*");
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        return Result.success();
    }

    /**
     * 清理缓存数据
     */
    private void clearCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
