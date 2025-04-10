package com.CC.controller.admin;

import com.CC.dto.SetmealDTO;
import com.CC.dto.SetmealPageQueryDTO;
import com.CC.result.PageResult;
import com.CC.result.Result;
import com.CC.service.SetmealService;
import com.CC.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags="套餐相关接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     */
    @PostMapping
    @ApiOperation("添加套餐")
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")//key:setmealCache::categoryId
    public Result save(@RequestBody SetmealDTO setmealDTO){
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
     @ApiOperation("分页查询")
    public Result page(SetmealPageQueryDTO setmealPageQueryDTO){
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)//清理所有缓存数据
    public Result delete(@RequestParam List<Long> ids){
        log.info("套餐批量删除:{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 启用或停用套餐
     * @param statuts
     * @param id
     * @return
     */
    @ApiOperation("启用或停用套餐")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, long id){
        setmealService.startOrStop(status,id);
        return Result.success();
    };
}
