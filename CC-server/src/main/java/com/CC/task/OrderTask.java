package com.CC.task;

import com.CC.entity.Orders;
import com.CC.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    //@Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        //查询是否有超时订单
        LocalDateTime failedTime = LocalDateTime.now().plusMinutes(1);
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT,failedTime);

        if(list!=null&&list.size()>0){
            for(Orders order:list){
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    /**
     * 处理一直处于派送的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理超时订单:{}", LocalDateTime.now());

        //查询是否有超时订单
        LocalDateTime failedTime = LocalDateTime.now().plusHours(-1);
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS,failedTime);

        if(list!=null&&list.size()>0){
            for(Orders order:list){
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
