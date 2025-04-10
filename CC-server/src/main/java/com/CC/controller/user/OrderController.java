package com.CC.controller.user;
import com.CC.dto.OrdersPaymentDTO;
import com.CC.dto.OrdersSubmitDTO;
import com.CC.result.PageResult;
import com.CC.result.Result;
import com.CC.service.OrderService;
import com.CC.vo.OrderPaymentVO;
import com.CC.vo.OrderSubmitVO;
import com.CC.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单接口")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("历史订单")
    public Result<PageResult> page(int page, int pageSize, Integer status){
        log.info("历史订单");
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 订单详情页
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("订单详情页")
    public Result<OrderVO> detail(@PathVariable Long id){
        OrderVO orderVO = orderService.detail(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id){
        orderService.cancel(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetiton(@PathVariable Long id){
        log.info("再来一单");
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id){
        orderService.reminder(id);
        return Result.success();
    }
}
