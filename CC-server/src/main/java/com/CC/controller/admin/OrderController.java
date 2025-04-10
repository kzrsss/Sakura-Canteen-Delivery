package com.CC.controller.admin;
import com.CC.dto.*;
import com.CC.result.PageResult;
import com.CC.result.Result;
import com.CC.service.OrderService;
import com.CC.vo.OrderStatisticsVO;
import com.CC.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "管理端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索");
        PageResult pageResult = orderService.pageQuery4Admin(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> orderStatic(){
        OrderStatisticsVO orderStatisticsVO = orderService.orderStatic();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 订单详情页
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("订单详情页")
    public Result<OrderVO> detail(@PathVariable Long id){
        OrderVO orderVO = orderService.detail(id);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception{
        orderService.cancelByAdmin(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersRejectionDTO ordersCancelDTO) throws Exception{
        orderService.rejectByAdmin(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("接单")
    public Result delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("接单")
    public Result complete(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
